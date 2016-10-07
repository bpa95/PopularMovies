package io.github.bpa95.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import io.github.bpa95.popularmovies.fragments.MoviesGridFragment;
import io.github.bpa95.popularmovies.R;

public class MovieCursorAdapter extends CursorAdapter {
    private static final String LOG_TAG = MovieCursorAdapter.class.getSimpleName();

    public MovieCursorAdapter(Context context) {
        super(context, null, 0);
    }

    /**
     * Defines a class that hold resource IDs of each item layout
     * row to prevent having to look them up each time data is
     * bound to a row.
     */
    private class ViewHolder {
        ImageView poster;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.grid_movie_item, parent, false);
        final ViewHolder holder = new ViewHolder();
        holder.poster = (ImageView) view.findViewById(R.id.movies_grid_image_view_item);
        view.setTag(holder);
        return view;
    }

    private Uri getPosterPathFromCursor(Cursor cursor) {
        return Uri.parse(cursor.getString(MoviesGridFragment.COLUMN_POSTER_PATH));
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Uri posterPath = getPosterPathFromCursor(cursor);
        final ViewHolder holder = (ViewHolder) view.getTag();
        Picasso.with(context).load(posterPath).into(holder.poster);
    }
}
