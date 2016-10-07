package io.github.bpa95.popularmovies.objects;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.github.bpa95.popularmovies.BuildConfig;
import io.github.bpa95.popularmovies.R;
import io.github.bpa95.popularmovies.data.MoviesContract.MovieEntry;

public class Movie implements Parcelable {
    private static final String LOG_TAG = Movie.class.getSimpleName();
    public int id;
    public Uri posterPath;
    public String title;
    public String releaseDate;
    public double popularity;
    public double voteAverage;
    public String overview;

    private static final String MOVIES_BASE_URL = "http://api.themoviedb.org/3";
    private static final String MOVIE_PATH = "movie";
    private static final String VIDEOS_PATH = "videos";
    private static final String REVIEWS_PATH = "reviews";
    private static final String API_KEY_PARAM = "api_key";

    Movie() {
    }

    public Movie(JSONObject jsonMovie) throws JSONException {
        // These are the names of the JSON objects that need to be extracted
        final String TMDB_MOVIE_ID = "id";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_TITLE = "title";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_POPULARITY = "popularity";
        final String TMDB_VOTE_AVERAGE = "vote_average";
        final String TMDB_OVERVIEW = "overview";

        id = jsonMovie.getInt(TMDB_MOVIE_ID);
        posterPath = createPosterPath(jsonMovie.getString(TMDB_POSTER_PATH));
        title = jsonMovie.getString(TMDB_TITLE);
        releaseDate = parseDate(jsonMovie.getString(TMDB_RELEASE_DATE));
        popularity = jsonMovie.getDouble(TMDB_POPULARITY);
        voteAverage = jsonMovie.getDouble(TMDB_VOTE_AVERAGE);
        overview = jsonMovie.getString(TMDB_OVERVIEW);
    }

    private Uri createPosterPath(String path) {
        final String BASE_URL = "http://image.tmdb.org/t/p/";
        final String IMAGE_SIZE = "w185";

        return Uri.parse(BASE_URL).buildUpon()
                .appendEncodedPath(IMAGE_SIZE)
                .appendEncodedPath(path)
                .build();
    }

    public Movie(Cursor cursor) {
        id = cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID));
        posterPath = Uri.parse(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER_PATH)));
        title = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE));
        releaseDate = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE));
        popularity = cursor.getDouble(cursor.getColumnIndex(MovieEntry.COLUMN_POPULARITY));
        voteAverage = cursor.getDouble(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE));
        overview = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW));
    }

    public static void transferMoviesFromServerToLocalDb(Context context, ContentProviderClient provider)
            throws RemoteException, IOException, JSONException {
        HttpURLConnection urlConnection = null;

        String sortOrder = context.getString(R.string.pref_sortOrder_popular_value);
        URL url = Movie.constructUrl(sortOrder);

        try {
            urlConnection = performConnection(url);

            Movie[] movies = getMovieDataFromJson(getJsonString(urlConnection.getInputStream()));

            for (Movie movie : movies) {
                movie.insertMovieInDb(provider);
                movie.downloadAndInsertTrailersInDb(provider);
                movie.downloadAndInsertReviewsInDb(provider);
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private static HttpURLConnection performConnection(URL url) throws IOException {
        Log.d(LOG_TAG, "preforming connection to " + url);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();
        return urlConnection;
    }


    private Trailer[] getTrailersDataFromJson(String trailerJsonStr) throws IOException, JSONException {
        if (trailerJsonStr == null || trailerJsonStr.isEmpty()) {
            throw new IOException("Empty json");
        }

        final String TMDB_RESULTS = "results";

        JSONArray trailersJson = new JSONObject(trailerJsonStr)
                .getJSONArray(TMDB_RESULTS);

        int length = trailersJson.length();
        Trailer[] trailers = new Trailer[length];
        for (int i = 0; i < length; i++) {
            trailers[i] = new Trailer(trailersJson.getJSONObject(i), id);
        }

        return trailers;
    }

    private Review[] getReviewsDataFromJson(String reviewJsonStr) throws IOException, JSONException {
        if (reviewJsonStr == null || reviewJsonStr.isEmpty()) {
            throw new IOException("Empty json");
        }

        final String TMDB_RESULTS = "results";

        JSONArray reviewsJson = new JSONObject(reviewJsonStr)
                .getJSONArray(TMDB_RESULTS);

        int length = reviewsJson.length();
        Review[] reviews = new Review[length];
        for (int i = 0; i < length; i++) {
            reviews[i] = new Review(reviewsJson.getJSONObject(i), id);
        }

        return reviews;
    }

    private void downloadAndInsertTrailersInDb(ContentProviderClient provider) {
        HttpURLConnection urlConnection = null;

        try {
            URL url = constructTrailerUrl();
            urlConnection = performConnection(url);

            Trailer[] trailers = getTrailersDataFromJson(getJsonString(urlConnection.getInputStream()));

            for (Trailer trailer : trailers) {
                trailer.insertTrailerInDb(provider);
            }
        } catch (JSONException | RemoteException | IOException e) {
            Log.e(LOG_TAG, "Error inserting trailer", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private URL constructTrailerUrl() throws MalformedURLException {
        Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                .appendEncodedPath(MOVIE_PATH)
                .appendEncodedPath(Integer.toString(id))
                .appendEncodedPath(VIDEOS_PATH)
                .appendQueryParameter(API_KEY_PARAM, BuildConfig.MOVIE_DATABASE_API_KEY)
                .build();

        Log.v(LOG_TAG, builtUri.toString());

        return new URL(builtUri.toString());
    }

    private URL constructReviewUrl() throws MalformedURLException {
        Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                .appendEncodedPath(MOVIE_PATH)
                .appendEncodedPath(Integer.toString(id))
                .appendEncodedPath(REVIEWS_PATH)
                .appendQueryParameter(API_KEY_PARAM, BuildConfig.MOVIE_DATABASE_API_KEY)
                .build();

        Log.v(LOG_TAG, builtUri.toString());

        return new URL(builtUri.toString());
    }

    private void downloadAndInsertReviewsInDb(ContentProviderClient provider) {
        HttpURLConnection urlConnection = null;

        try {
            URL url = constructReviewUrl();
            urlConnection = performConnection(url);

            Review[] reviews = getReviewsDataFromJson(getJsonString(urlConnection.getInputStream()));

            for (Review review : reviews) {
                review.insertReviewInDb(provider);
            }
        } catch (JSONException | RemoteException | IOException e) {
            Log.e(LOG_TAG, "Error inserting review", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private static final String[] projection = new String[]{MovieEntry.COLUMN_MOVIE_ID};
    private static final String selection = MovieEntry.COLUMN_MOVIE_ID + " = ?";
    private static final String[] selectionArgs = new String[1];

    private void insertMovieInDb(ContentProviderClient provider) throws RemoteException {
        ContentValues cv = new ContentValues();
        cv.put(MovieEntry.COLUMN_MOVIE_ID, id);
        cv.put(MovieEntry.COLUMN_POSTER_PATH, posterPath.toString());
        cv.put(MovieEntry.COLUMN_TITLE, title);
        cv.put(MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
        cv.put(MovieEntry.COLUMN_POPULARITY, popularity);
        cv.put(MovieEntry.COLUMN_VOTE_AVERAGE, voteAverage);
        cv.put(MovieEntry.COLUMN_OVERVIEW, overview);

        selectionArgs[0] = cv.getAsString(MovieEntry.COLUMN_MOVIE_ID);
        Cursor cursor = provider.query(
                MovieEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
        if (cursor == null) {
            return;
        }
        if (!cursor.moveToFirst()) {
            provider.insert(MovieEntry.CONTENT_URI, cv);
        } else {
            provider.update(
                    MovieEntry.CONTENT_URI,
                    cv,
                    selection,
                    selectionArgs
            );
        }
        cursor.close();
    }

    /**
     * Extracts movie data from json string and returns array of movies with this data.
     *
     * @param moviesJsonStr json string to parse
     * @return array of movies
     * @throws JSONException if passed string is not appropriate json string
     * @throws IOException   if passed string is null or empty
     */
    private static Movie[] getMovieDataFromJson(String moviesJsonStr) throws JSONException, IOException {
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
     * Constructs the URL for the Movie Database query, which contains the json object
     * with movie data. Movies will be in order specified by parameter.
     *
     * @param sortOrder part of path which specify sort order
     * @return correct url to The Movie Database from which the json object with movie data can be fetched
     * @throws MalformedURLException should never happen
     */
    @NonNull
    private static URL constructUrl(final String sortOrder) throws MalformedURLException {
        Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                .appendEncodedPath(sortOrder)
                .appendQueryParameter(API_KEY_PARAM, BuildConfig.MOVIE_DATABASE_API_KEY)
                .build();

        Log.v(LOG_TAG, builtUri.toString());

        return new URL(builtUri.toString());
    }

    /**
     * Reads bytes from given input stream into string. It is assumed that
     * input stream contains correct json object.
     *
     * @param inputStream stream with correct json object
     * @return json string read from given stream
     * @throws IOException in case of errors in input stream
     */
    private static String getJsonString(InputStream inputStream) throws IOException {
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


    private static final String DB_DATE_FORMAT = "yyyy-MM-dd";
    private static final SimpleDateFormat dbDateFormat = new SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault());
    private static final String UI_DATE_FORMAT = "MMM d, yyyy";
    private static final SimpleDateFormat uiDateFormat = new SimpleDateFormat(UI_DATE_FORMAT, Locale.getDefault());

    private static String parseDate(String date) {
        String pDate;
        try {
            pDate = uiDateFormat.format(dbDateFormat.parse(date));
        } catch (ParseException e) {
            pDate = "Unknown";
        }
        return pDate;
    }

    private Movie(Parcel in) {
        id = in.readInt();
        posterPath = in.readParcelable(Uri.class.getClassLoader());
        title = in.readString();
        releaseDate = in.readString();
        popularity = in.readDouble();
        voteAverage = in.readDouble();
        overview = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeParcelable(posterPath, i);
        parcel.writeString(title);
        parcel.writeString(releaseDate);
        parcel.writeDouble(popularity);
        parcel.writeDouble(voteAverage);
        parcel.writeString(overview);
    }
}
