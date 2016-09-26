package io.github.bpa95.popularmovies.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import io.github.bpa95.popularmovies.BuildConfig;
import io.github.bpa95.popularmovies.Movie;
import io.github.bpa95.popularmovies.R;
import io.github.bpa95.popularmovies.data.MoviesContract;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class MovieIntentService extends IntentService {
    private static final String LOG_TAG = MovieIntentService.class.getSimpleName();

    private static final String ACTION_LOAD_MOVIES = "io.github.bpa95.popularmovies.service.action.LOAD_MOVIES";

    private static final String EXTRA_SORT_ORDER = "io.github.bpa95.popularmovies.service.extra.SORT_ORDER";

    public MovieIntentService() {
        super("MovieIntentService");
    }

    /**
     * Starts this service to load movies from TMDB. If the service
     * is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void loadMovies(Context context, String sortOrder) {
        Intent intent = new Intent(context, MovieIntentService.class);
        intent.setAction(ACTION_LOAD_MOVIES);
        intent.putExtra(EXTRA_SORT_ORDER, sortOrder);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_LOAD_MOVIES.equals(action)) {
                final String sortOrder = intent.getStringExtra(EXTRA_SORT_ORDER);
                handleMoviesLoading(sortOrder);
            }
        }
    }

    /**
     * Handle movies loading in the provided background thread with the provided
     * sortOrder parameter.
     */
    private void handleMoviesLoading(String sortOrder) {
        HttpURLConnection urlConnection = null;

        String mErrorMessage;

        try {
            // Construct the URL for the Movie Database query
            URL url = constructUrl(sortOrder);

            if (!isOnline()) {
                mErrorMessage = getString(R.string.check_connection);
                return;
            }

            // Create the request to TheMovieDatabase, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            Movie[] movies = getMovieDataFromJson(getJsonString(urlConnection.getInputStream()));

            ContentValues[] cvArray = new ContentValues[movies.length];
            for (int i = 0; i < movies.length; i++) {
                Movie movie = movies[i];
                ContentValues cv = new ContentValues();
                cv.put(MoviesContract.MovieEntry.COLUMN_MOVIE_ID, movie.id);
                cv.put(MoviesContract.MovieEntry.COLUMN_POSTER_PATH, movie.posterPath.toString());
                cv.put(MoviesContract.MovieEntry.COLUMN_TITLE, movie.title);
                cv.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, movie.releaseDate);
                cv.put(MoviesContract.MovieEntry.COLUMN_POPULARITY, movie.popularity);
                cv.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.voteAverage);
                cv.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, movie.overview);

                cvArray[i] = cv;
            }
//            if (cvArray.length > 0) {
//                mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, cvArray);
//            }
            ContentResolver cr = getContentResolver();
            String[] projection = new String[]{MoviesContract.MovieEntry.COLUMN_MOVIE_ID};
            String selection = MoviesContract.MovieEntry.COLUMN_MOVIE_ID + " = ?";
            for (ContentValues cv : cvArray) {
                String[] selectionArgs = new String[]{cv.getAsString(MoviesContract.MovieEntry.COLUMN_MOVIE_ID)};
                Cursor cursor = cr.query(
                        MoviesContract.MovieEntry.CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        null
                );
                if (cursor == null) {
                    continue;
                }
                if (!cursor.moveToFirst()) {
                    cr.insert(MoviesContract.MovieEntry.CONTENT_URI, cv);
                } else {
                    cr.update(
                            MoviesContract.MovieEntry.CONTENT_URI,
                            cv,
                            selection,
                            selectionArgs
                    );
                }
                cursor.close();
            }
        } catch (IOException | JSONException e) {
            Log.e(LOG_TAG, "Error ", e);
            mErrorMessage = getString(R.string.error_fetching);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    /**
     * Reads bytes from given input stream into string. It is assumed that
     * input stream contains correct json object.
     *
     * @param inputStream stream with correct json object
     * @return json string read from given stream
     * @throws IOException in case of errors in input stream
     */
    private String getJsonString(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            StringBuilder buffer = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append('\n');
            }

            return buffer.toString();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    /**
     * Extracts movie data from json string and returns array of movies with this data.
     *
     * @param moviesJsonStr json string to parse
     * @return array of movies
     * @throws JSONException if passed string is not appropriate json string
     * @throws IOException   if passed string is null or empty
     */
    private Movie[] getMovieDataFromJson(String moviesJsonStr) throws JSONException, IOException {
        if (moviesJsonStr == null || moviesJsonStr.isEmpty()) {
            throw new IOException("Empty json");
        }

        final String TMDB_RESULTS = "results";

        JSONArray moviesJson = new JSONObject(moviesJsonStr)
                .getJSONArray(TMDB_RESULTS);

        int length = moviesJson.length();
        Movie[] movies = new Movie[length];
        for (int i = 0; i < length; i++) {
            movies[i] = new Movie(moviesJson.getJSONObject(i));
        }

        return movies;
    }

    /**
     * Checks internet connection.
     *
     * @return true if there is internet connection, false - otherwise
     */
    // uses solution from stackoverflow
    // http://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-timeouts
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Constructs the URL for the Movie Database query, which contains the json object
     * with movie data. Movies will be in order specified by parameter.
     *
     * @param SORT_ORDER part of path which specify sort order
     * @return correct url to The Movie Database from which the json object with movie data can be fetched
     * @throws MalformedURLException should never happen
     */
    @NonNull
    private URL constructUrl(final String SORT_ORDER) throws MalformedURLException {
        final String MOVIES_BASE_URL = "http://api.themoviedb.org/3";
        final String API_KEY_PARAM = "api_key";

        Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                .appendEncodedPath(SORT_ORDER)
                .appendQueryParameter(API_KEY_PARAM, BuildConfig.MOVIE_DATABASE_API_KEY)
                .build();

        Log.v(LOG_TAG, builtUri.toString());

        return new URL(builtUri.toString());
    }

    public static class AlarmReceiver extends BroadcastReceiver {
        public static final String EXTRA_SORT_ORDER = "io.github.bpa95.popularmovies.service.AlarmReceiver.extra.SORT_ORDER";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Service starting");
            String sortOrder = intent.getStringExtra(EXTRA_SORT_ORDER);
            MovieIntentService.loadMovies(context, sortOrder);
            Log.d(LOG_TAG, "Service started");
        }
    }
}
