package io.github.bpa95.popularmovies;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.Arrays;

/**
 * A fragment containing the grid view of movies
 */
public class MoviesGridFragment extends Fragment {

    private MovieAdapter mMovieAdapter;

    public MoviesGridFragment() {
    }

    private Movie[] movies = new Movie[]{
            new Movie(R.drawable.interstellar),
            new Movie(R.drawable.interstellar),
            new Movie(R.drawable.interstellar),
            new Movie(R.drawable.interstellar),
            new Movie(R.drawable.interstellar)
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movies_grid, container, false);

        mMovieAdapter = new MovieAdapter(getActivity(), Arrays.asList(movies));

        // Get a reference to the GridView, and attach this adapter to it.
        GridView listView = (GridView) rootView.findViewById(R.id.movies_grid_view);
        listView.setAdapter(mMovieAdapter);

        return rootView;
    }
}
