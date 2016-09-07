package io.github.bpa95.popularmovies;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import io.github.bpa95.popularmovies.data.MoviesContract;

/**
 * A fragment containing the grid view of movies.
 */
public class MoviesGridFragment extends Fragment {

    private static final String LOG_TAG = MoviesGridFragment.class.getSimpleName();

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movies_grid, container, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrderPref = prefs.getString(getString(R.string.pref_sortOrder_key),
                getString(R.string.pref_sortOrder_popular_value));
        String sortOrder = MoviesContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        if (sortOrderPref.equals(getString(R.string.pref_sortOrder_topRated_value))) {
            sortOrder = MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
        }
        Uri uri = MoviesContract.MovieEntry.CONTENT_URI;

        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, sortOrder);

        mMovieAdapter = new MovieCursorAdapter(getActivity(), cursor, 0);

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
}
