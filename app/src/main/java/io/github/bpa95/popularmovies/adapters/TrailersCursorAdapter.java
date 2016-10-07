package io.github.bpa95.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import io.github.bpa95.popularmovies.R;
import io.github.bpa95.popularmovies.fragments.TrailerListFragment;

public class TrailersCursorAdapter extends CursorAdapter {
    private static final String LOG_TAG = TrailersCursorAdapter.class.getSimpleName();

    public TrailersCursorAdapter(Context context) {
        super(context, null, 0);
    }

    /**
     * Defines a class that hold resource IDs of each item layout
     * row to prevent having to look them up each time data is
     * bound to a row.
     */
    private class ViewHolder {
        TextView trailerName;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.trailer_list_item, parent, false);
        final ViewHolder holder = new ViewHolder();
        holder.trailerName = (TextView) view.findViewById(R.id.trailer_name_text_view);
        view.setTag(holder);
        return view;
    }

    private String getTrailerNameFromCursor(Cursor cursor) {
        return cursor.getString(TrailerListFragment.COLUMN_NAME);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String trailerName = getTrailerNameFromCursor(cursor);
        final ViewHolder holder = (ViewHolder) view.getTag();
        holder.trailerName.setText(trailerName);
    }
}
