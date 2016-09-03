package io.github.bpa95.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

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

/**
 * Fetches from server the json with movies, parses it, and fills the adapter with Movies objects.
 */
public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
    private String mErrorMessage;

    private MovieAdapter mMovieAdapter;
    private final Context mContext;

    public FetchMoviesTask(Context mContext, MovieAdapter mMovieAdapter) {
        this.mContext = mContext;
        this.mMovieAdapter = mMovieAdapter;
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
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
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

    @Override
    protected Movie[] doInBackground(String... strings) {

        HttpURLConnection urlConnection = null;

        final String SORT_ORDER;
        if (strings != null && strings.length > 0) {
            SORT_ORDER = strings[0];
        } else {
            mErrorMessage = mContext.getString(R.string.never_happens);
            Log.e(LOG_TAG, "strings is bad");
            return null;
        }

        try {
            // Construct the URL for the Movie Database query
            URL url = constructUrl(SORT_ORDER);

            if (!isOnline()) {
                mErrorMessage = mContext.getString(R.string.check_connection);
                return null;
            }

            // Create the request to TheMovieDatabase, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            return getMovieDataFromJson(getJsonString(urlConnection.getInputStream()));
        } catch (IOException | JSONException e) {
            Log.e(LOG_TAG, "Error ", e);
            mErrorMessage = mContext.getString(R.string.error_fetching);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(Movie[] movies) {
        if (movies != null) {
            mMovieAdapter.clear();
            mMovieAdapter.addAll(movies);
        } else {
            Toast.makeText(mContext, mErrorMessage, Toast.LENGTH_SHORT).show();
        }
    }
}