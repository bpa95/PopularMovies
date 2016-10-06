package io.github.bpa95.popularmovies;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.CursorAdapter;

import io.github.bpa95.popularmovies.data.MoviesContract;
import io.github.bpa95.popularmovies.data.MoviesContract.TrailerEntry;

import static io.github.bpa95.popularmovies.DetailFragment.DETAIL_URI;

public class TrailerListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mUri;

    private static final int DETAILS_LOADER_ID = 0;
    private static final int TRAILERS_LOADER_ID = 1;
    private static final String LOG_TAG = TrailerListFragment.class.getSimpleName();

    private CursorAdapter mTrailerAdapter;

    public TrailerListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTrailerAdapter = new TrailersCursorAdapter(getActivity());
        setListAdapter(mTrailerAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mUri = getActivity().getIntent().getData();
        Bundle args = getArguments();
        if (mUri == null && args != null) {
            mUri = args.getParcelable(DETAIL_URI);
        }
        if (mUri != null) {
            getLoaderManager().initLoader(DETAILS_LOADER_ID, null, this);
            getLoaderManager().initLoader(TRAILERS_LOADER_ID, null, this);
        }
        super.onActivityCreated(savedInstanceState);
    }

    private static final String[] TRAILER_COLUMNS = new String[]{
            TrailerEntry.TABLE_NAME + "." + TrailerEntry._ID,
            TrailerEntry.COLUMN_NAME
    };

    // These indices are tied to TRAILER_COLUMNS.  If TRAILER_COLUMNS changes, these
    // must change.
    static final int COLUMN_ID = 0;
    static final int COLUMN_NAME = 1;

    private final String[] MOVIE_COLUMNS = new String[]{
            MoviesContract.MovieEntry.COLUMN_MOVIE_ID,
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MovieEntry.COLUMN_OVERVIEW,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH,
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
        switch (id) {
            case DETAILS_LOADER_ID:
                return new CursorLoader(
                        getActivity(),
                        mUri,
                        MOVIE_COLUMNS,
                        null,
                        null,
                        null
                );
            case TRAILERS_LOADER_ID:
                Uri uri = TrailerEntry.buildTrailersByMovieIdUri(ContentUris.parseId(mUri));
                return new CursorLoader(getActivity(), uri, TRAILER_COLUMNS, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case TRAILERS_LOADER_ID:
                mTrailerAdapter.swapCursor(data);
                break;
            case DETAILS_LOADER_ID:
//                View rootView = getView();
//                if (rootView == null || data == null) {
//                    return;
//                }
//                if (!data.moveToFirst()) {
//                    Log.d(LOG_TAG, "Empty cursor");
//                    return;
//                }
//                ((TextView) rootView.findViewById(R.id.detail_title)).setText(
//                        data.getString(COLUMN_TITLE));
//                ((TextView) rootView.findViewById(R.id.detail_release_date))
//                        .setText(String.format(Locale.getDefault(), "Release date:%n%s",
//                                data.getString(COLUMN_RELEASE_DATE)));
//                ((TextView) rootView.findViewById(R.id.detail_rating))
//                        .setText(String.format(Locale.getDefault(), "Rating:%n%1.1f/10",
//                                data.getDouble(COLUMN_VOTE_AVERAGE)));
//                ((TextView) rootView.findViewById(R.id.detail_overview)).setText(
//                        data.getString(COLUMN_OVERVIEW));
//
//                ImageView imageView = (ImageView) rootView.findViewById(R.id.detail_poster);
//                Picasso.with(getActivity()).load(data.getString(COLUMN_POSTER_PATH)).into(imageView);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case TRAILERS_LOADER_ID:
                mTrailerAdapter.swapCursor(null);
                break;
        }
    }
}
