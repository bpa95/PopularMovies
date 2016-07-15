package io.github.bpa95.popularmovies;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
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
import java.util.ArrayList;

/**
 * A fragment containing the grid view of movies.
 */
public class MoviesGridFragment extends Fragment {

    private MovieAdapter mMovieAdapter;
    private AdapterView.OnItemClickListener mListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intentDetail = new Intent(getActivity(), DetailActivity.class);
            intentDetail.putExtra(DetailFragment.EXTRA_MOVIE, mMovieAdapter.getItem(i));
            startActivity(intentDetail);
        }
    };

    public MoviesGridFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movies_grid, container, false);

        mMovieAdapter = new MovieAdapter(getActivity(), new ArrayList<Movie>());

        // Get a reference to the GridView, and attach this adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.movies_grid_view);
        gridView.setAdapter(mMovieAdapter);

        gridView.setOnItemClickListener(mListener);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateGrid();
    }

    /**
     * Fills grid view with posters of movies fetched from server.
     */
    private void updateGrid() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort_order = prefs.getString(getString(R.string.pref_sortOrder_key),
                getString(R.string.pref_sortOrder_popular_value));
        new FetchMoviesTask().execute(sort_order);
    }

    /**
     * Fetches from server the json with movies, parses it, and fills the adapter with Movies objects.
     */
    private class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
        private String mErrorMessage;


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
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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
         * Constructs url from which the json object with movie data can be fetched.
         * Movies will be in order specified by parameter.
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

        @Override
        protected Movie[] doInBackground(String... strings) {

            HttpURLConnection urlConnection = null;

            // Will contain the raw JSON response as a string
            String moviesJsonStr = null;

            final String SORT_ORDER;
            if (strings != null && strings.length > 0) {
                SORT_ORDER = strings[0];
            } else {
                mErrorMessage = "Should never happen";
                Log.e(LOG_TAG, "strings is bad");
                return null;
            }

            try {
                // Construct the URL for the Movie Database query
                URL url = constructUrl(SORT_ORDER);

                if (!isOnline()) {
                    mErrorMessage = "Check internet connection";
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
                mErrorMessage = "Error while fetching data";
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
                Toast.makeText(getActivity(), mErrorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
