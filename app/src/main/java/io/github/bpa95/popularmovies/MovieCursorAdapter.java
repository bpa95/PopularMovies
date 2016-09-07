package io.github.bpa95.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class MovieCursorAdapter extends CursorAdapter {
    private static final String LOG_TAG = MovieCursorAdapter.class.getSimpleName();

    public MovieCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.grid_movie_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Movie movie = new Movie(cursor);

        ImageView imageView = (ImageView) view.findViewById(R.id.movies_grid_image_view_item);
        Picasso.with(context).load(movie.posterPath).into(imageView);
    }
}
