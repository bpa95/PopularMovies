package io.github.bpa95.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import io.github.bpa95.popularmovies.BuildConfig;
import io.github.bpa95.popularmovies.MainActivity;
import io.github.bpa95.popularmovies.Movie;
import io.github.bpa95.popularmovies.R;
import io.github.bpa95.popularmovies.data.MoviesContract;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String LOG_TAG = SyncAdapter.class.getSimpleName();
    private static final long MINUTE = 60L;
    private static final long HOUR = 60 * MINUTE;
    private static final long SYNC_INTERVAL = 24 * HOUR;
//    private static final long SYNC_INTERVAL = 24 * HOUR;
    private static final long SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    ContentResolver mContentResolver;

    private static final String EXTRA_SORT_ORDER = "io.github.bpa95.popularmovies.sync.extra.SORT_ORDER";

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        HttpURLConnection urlConnection = null;
        Log.d(LOG_TAG, "onPerformSync called");

        try {
            // Construct the URL for the Movie Database query
            String sortOrder = getContext().getString(R.string.pref_sortOrder_popular_value);
            URL url = constructUrl(sortOrder);

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
            String[] projection = new String[]{MoviesContract.MovieEntry.COLUMN_MOVIE_ID};
            String selection = MoviesContract.MovieEntry.COLUMN_MOVIE_ID + " = ?";
            for (ContentValues cv : cvArray) {
                String[] selectionArgs = new String[]{cv.getAsString(MoviesContract.MovieEntry.COLUMN_MOVIE_ID)};
                Cursor cursor = provider.query(
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
                    provider.insert(MoviesContract.MovieEntry.CONTENT_URI, cv);
                } else {
                    provider.update(
                            MoviesContract.MovieEntry.CONTENT_URI,
                            cv,
                            selection,
                            selectionArgs
                    );
                }
                cursor.close();
            }
        } catch (IOException | JSONException | RemoteException e) {
            Log.e(LOG_TAG, "Error ", e);
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
     * Constructs the URL for the Movie Database query, which contains the json object
     * with movie data. Movies will be in order specified by parameter.
     *
     * @param sortOrder part of path which specify sort order
     * @return correct url to The Movie Database from which the json object with movie data can be fetched
     * @throws MalformedURLException should never happen
     */
    @NonNull
    private URL constructUrl(final String sortOrder) throws MalformedURLException {
        final String MOVIES_BASE_URL = "http://api.themoviedb.org/3";
        final String API_KEY_PARAM = "api_key";

        Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                .appendEncodedPath(sortOrder)
                .appendQueryParameter(API_KEY_PARAM, BuildConfig.MOVIE_DATABASE_API_KEY)
                .build();

        Log.v(LOG_TAG, builtUri.toString());

        return new URL(builtUri.toString());
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Log.d(LOG_TAG, "syncImmediately Called.");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        Account account = getSyncAccount(context);
        final String authority = context.getString(R.string.content_authority);
        bundle.putString(EXTRA_SORT_ORDER, getSortOrder(context));
        ContentResolver.requestSync(account, authority, bundle);
        Log.d(LOG_TAG, "syncImmediately finished.");
    }

    private static String getSortOrder(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int sortOrderPref = prefs.getInt(MainActivity.PREF_SORT_ORDER,
                MainActivity.PREF_SORT_BY_POPULARITY);
        String sortOrder = context.getString(R.string.pref_sortOrder_popular_value);
        if (sortOrderPref == MainActivity.PREF_SORT_BY_RATING) {
            sortOrder = context.getString(R.string.pref_sortOrder_topRated_value);
        }
        return sortOrder;
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

            /*
             * Add the account and account type, no password or user data
             * If successful, return the Account object, otherwise report an error.
             */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                Log.d(LOG_TAG, "getSyncAccount inside if if");
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, long syncInterval, long flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_SORT_ORDER, getSortOrder(context));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(bundle).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, bundle, syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        // Since we've created an account
        configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        // Without calling setSyncAutomatically, our periodic sync will not be enabled.
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        // Finally, let's do a sync to get things started
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
