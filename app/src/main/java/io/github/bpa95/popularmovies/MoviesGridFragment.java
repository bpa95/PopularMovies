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
import java.net.URL;
import java.util.ArrayList;

/**
 * A fragment containing the grid view of movies
 */
public class MoviesGridFragment extends Fragment {

    private MovieAdapter mMovieAdapter;
    private AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
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

        gridView.setOnItemClickListener(listener);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateGrid();
    }

    private void updateGrid() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort_order = prefs.getString(getString(R.string.pref_sortOrder_key),
                getString(R.string.pref_sortOrder_popular_value));
        new FetchMoviesTask().execute(sort_order);
    }

    private class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
        private String errorMessage;


        private Movie[] getMovieDataFromJson(String moviesJsonStr) throws JSONException {

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

        // uses solution from stackoverflow
        // http://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-timeouts
        public boolean isOnline() {
            ConnectivityManager cm =
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }

        @Override
        protected Movie[] doInBackground(String... strings) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string
            String moviesJsonStr = null;

            final String SORT_ORDER;
            if (strings != null && strings.length > 0) {
                SORT_ORDER = strings[0];
            } else {
                errorMessage = "Should never happen";
                Log.e(LOG_TAG, "strings is bad");
                return null;
            }

            try {
                // Construct the URL for the Movie Database query
                final String MOVIES_BASE_URL = "http://api.themoviedb.org/3";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendEncodedPath(SORT_ORDER)
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.MOVIE_DATABASE_API_KEY)
                        .build();

                Log.v(LOG_TAG, builtUri.toString());

                URL url = new URL(builtUri.toString());

                if (!isOnline()) {
                    errorMessage = "Check internet connection";
                    return null;
                }

                // Create the request to TheMovieDatabase, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    errorMessage = "Error while fetching data";
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line).append('\n');
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    errorMessage = "Error while fetching data";
                    return null;
                }
                moviesJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                errorMessage = "Error while fetching data";
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                errorMessage = "Error while fetching data";
                return null;
            }
        }

        @Override
        protected void onPostExecute(Movie[] movies) {
            if (movies != null) {
                mMovieAdapter.clear();
                mMovieAdapter.addAll(movies);
            } else {
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
