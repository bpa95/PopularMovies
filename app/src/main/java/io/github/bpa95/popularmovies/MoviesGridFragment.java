package io.github.bpa95.popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import io.github.bpa95.popularmovies.data.MoviesContract;

/**
 * A fragment containing the grid view of movies.
 */
public class MoviesGridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MoviesGridFragment.class.getSimpleName();

    public static final int LOADER_ID = 0;

    private MovieCursorAdapter mMovieAdapter;
//    private AdapterView.OnItemClickListener mListener = new AdapterView.OnItemClickListener() {
//        @Override
//        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//            Intent intentDetail = new Intent(getActivity(), DetailActivity.class);
//            intentDetail.putExtra(DetailFragment.EXTRA_MOVIE, mMovieAdapter.getItem(i));
//            startActivity(intentDetail);
//        }
//    };

    public MoviesGridFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movies_grid, container, false);

        mMovieAdapter = new MovieCursorAdapter(getActivity(), null, 0);

        // Get a reference to the GridView, and attach this adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.movies_grid_view);
        gridView.setAdapter(mMovieAdapter);

//        gridView.setOnItemClickListener(mListener);

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
        String sortOrder = prefs.getString(getString(R.string.pref_sortOrder_key),
                getString(R.string.pref_sortOrder_popular_value));
        new FetchMoviesTask(getActivity()).execute(sortOrder);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != LOADER_ID) {
            return null;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrderPref = prefs.getString(getString(R.string.pref_sortOrder_key),
                getString(R.string.pref_sortOrder_popular_value));
        String sortOrder = MoviesContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        if (sortOrderPref.equals(getString(R.string.pref_sortOrder_topRated_value))) {
            sortOrder = MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
        }
        Uri uri = MoviesContract.MovieEntry.CONTENT_URI;

        Log.d(LOG_TAG, "onCreateLoader");

        return new CursorLoader(getActivity(), uri, null, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieAdapter.swapCursor(data);
        Log.d(LOG_TAG, "onLoadFinished");

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
        Log.d(LOG_TAG, "onLoaderReset");

    }
}
