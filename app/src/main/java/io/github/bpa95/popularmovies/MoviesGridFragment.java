package io.github.bpa95.popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import io.github.bpa95.popularmovies.data.MoviesContract;
import io.github.bpa95.popularmovies.data.MoviesContract.MovieEntry;
import io.github.bpa95.popularmovies.sync.SyncAdapter;

/**
 * A fragment containing the grid view of movies.
 */
public class MoviesGridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MoviesGridFragment.class.getSimpleName();

    private static final int LOADER_ID = 0;

    private int mPosition = 0;

    private static final String SELECTED_KEY = "selected";

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        void onMovieSelected(Uri uri);
    }

    private MovieCursorAdapter mMovieAdapter;
    private final AdapterView.OnItemClickListener mListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
            Cursor cursor = (Cursor) adapterView.getItemAtPosition(pos);
            ((Callback) getActivity()).
                    onMovieSelected(
                            MoviesContract.MovieEntry.buildMovieUri(cursor.getInt(COLUMN_ID))
                    );
            mPosition = pos;
        }
    };

    public MoviesGridFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID, null, this);
        updateData();
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movies_grid, container, false);

        mMovieAdapter = new MovieCursorAdapter(getActivity());

        // Get a reference to the GridView, and attach this adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.movies_grid_view);
        gridView.setAdapter(mMovieAdapter);

        gridView.setOnItemClickListener(mListener);

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        gridView.smoothScrollToPosition(mPosition);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateGrid();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_KEY, mPosition);
    }

    public void updateGrid() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    public void updateData() {
        SyncAdapter.syncImmediately(getActivity());
    }

    private static final String[] MOVIE_COLUMNS = new String[]{
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_POSTER_PATH
    };

    // These indices are tied to MOVIE_COLUMNS.  If MOVIE_COLUMNS changes, these
    // must change.
    static final int COLUMN_ID = 0;
    static final int COLUMN_POSTER_PATH = 1;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != LOADER_ID) {
            return null;
        }
        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);

        Uri uri = MoviesContract.MovieEntry.CONTENT_URI;

        boolean favorite = prefs.getBoolean(MainActivity.PREF_FAVORITE, false);
        if (favorite) {
            uri = MoviesContract.FavoriteEntry.CONTENT_URI;
        }

        int sortOrderPref = prefs.getInt(MainActivity.PREF_SORT_ORDER,
                MainActivity.PREF_SORT_BY_POPULARITY);
        String sortOrder = MoviesContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        if (sortOrderPref == MainActivity.PREF_SORT_BY_RATING) {
            sortOrder = MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
        }

        return new CursorLoader(getActivity(), uri, MOVIE_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }
}
