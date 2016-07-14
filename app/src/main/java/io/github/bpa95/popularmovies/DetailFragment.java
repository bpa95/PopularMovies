package io.github.bpa95.popularmovies;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Locale;

public class DetailFragment extends Fragment {
    public static final String EXTRA_MOVIE = "io.github.bpa95.popularmovies.EXTRA_MOVIE";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Movie movie = getActivity().getIntent().getParcelableExtra(EXTRA_MOVIE);

        ((TextView) rootView.findViewById(R.id.detail_title)).setText(movie.title);
        ((TextView) rootView.findViewById(R.id.detail_release_date))
                .setText(String.format(Locale.getDefault(), "Release date:%n%s", movie.releaseDate));
        ((TextView) rootView.findViewById(R.id.detail_rating))
                .setText(String.format(Locale.getDefault(), "Rating:%n%1.1f/10", movie.voteAverage));
        ((TextView) rootView.findViewById(R.id.detail_overview)).setText(movie.overview);

        ImageView imageView = (ImageView) rootView.findViewById(R.id.detail_poster);
        Picasso.with(getActivity()).load(movie.posterPath).into(imageView);

        return rootView;
    }
}
