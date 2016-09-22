package io.github.bpa95.popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Locale;

import io.github.bpa95.popularmovies.data.MoviesContract.FavoriteEntry;
import io.github.bpa95.popularmovies.data.MoviesContract.MovieEntry;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String EXTRA_MOVIE = "io.github.bpa95.popularmovies.EXTRA_MOVIE";

    private static final int LOADER_ID = 0;
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    static final String DETAIL_URI = "detail_uri";

    private Uri mUri;

    private boolean mFavorite;

    private final View.OnClickListener favoriteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ContentValues cv = new ContentValues();
            cv.put(FavoriteEntry.COLUMN_FAVORITE_ID, ContentUris.parseId(mUri));
            getActivity().getContentResolver().insert(FavoriteEntry.CONTENT_URI, cv);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        view.findViewById(R.id.mark_as_favorite_button).setOnClickListener(favoriteListener);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mUri = getActivity().getIntent().getData();
        Bundle args = getArguments();
        if (mUri == null && args != null) {
            mUri = args.getParcelable(DETAIL_URI);
        }
        if (mUri != null) {
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }
        super.onActivityCreated(savedInstanceState);
    }

    private final String[] MOVIE_COLUMNS = new String[]{
            MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieEntry.COLUMN_OVERVIEW,
            MovieEntry.COLUMN_POSTER_PATH,
    };

    // These indices are tied to MOVIE_COLUMNS.  If MOVIE_COLUMNS changes, these
    // must change.
    static final int COLUMN_MOVIE_ID = 0;
    static final int COLUMN_TITLE = 1;
    static final int COLUMN_RELEASE_DATE = 2;
    static final int COLUMN_VOTE_AVERAGE = 3;
    static final int COLUMN_OVERVIEW = 4;
    static final int COLUMN_POSTER_PATH = 5;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                mUri,
                MOVIE_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        View rootView = getView();
        if (rootView == null || data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            Log.d(LOG_TAG, "Empty cursor");
            return;
        }
        ((TextView) rootView.findViewById(R.id.detail_title)).setText(
                data.getString(COLUMN_TITLE));
        ((TextView) rootView.findViewById(R.id.detail_release_date))
                .setText(String.format(Locale.getDefault(), "Release date:%n%s",
                        data.getString(COLUMN_RELEASE_DATE)));
        ((TextView) rootView.findViewById(R.id.detail_rating))
                .setText(String.format(Locale.getDefault(), "Rating:%n%1.1f/10",
                        data.getDouble(COLUMN_VOTE_AVERAGE)));
        ((TextView) rootView.findViewById(R.id.detail_overview)).setText(
                data.getString(COLUMN_OVERVIEW));

        ImageView imageView = (ImageView) rootView.findViewById(R.id.detail_poster);
        Picasso.with(getActivity()).load(data.getString(COLUMN_POSTER_PATH)).into(imageView);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
