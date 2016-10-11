package io.github.bpa95.popularmovies.fragments;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Locale;

import io.github.bpa95.popularmovies.R;
import io.github.bpa95.popularmovies.adapters.TrailersCursorAdapter;
import io.github.bpa95.popularmovies.data.MoviesContract;
import io.github.bpa95.popularmovies.data.MoviesContract.FavoriteEntry;
import io.github.bpa95.popularmovies.data.MoviesContract.TrailerEntry;

import static java.lang.String.format;

public class TrailerListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mUri;

    public static final String DETAIL_URI = "detail_uri";

    private static final int DETAILS_LOADER_ID = 0;
    private static final int TRAILERS_LOADER_ID = 1;
    private static final int FAVORITE_LOADER_ID = 2;
    private static final String LOG_TAG = TrailerListFragment.class.getSimpleName();

    private CursorAdapter mTrailerAdapter;

    private View mHeaderView;

    private ImageButton mButtonStar;

    public TrailerListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTrailerAdapter = new TrailersCursorAdapter(getActivity());
        setListAdapter(mTrailerAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mHeaderView = inflater.inflate(R.layout.movie_detail_item, null);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (mHeaderView != null) {
            getListView().addHeaderView(mHeaderView, null, false);
        }
        mUri = getActivity().getIntent().getData();
        Bundle args = getArguments();
        if (mUri == null && args != null) {
            mUri = args.getParcelable(DETAIL_URI);
        }
        if (mUri != null) {
            getLoaderManager().initLoader(TRAILERS_LOADER_ID, null, this);
            getLoaderManager().initLoader(DETAILS_LOADER_ID, null, this);
            getLoaderManager().initLoader(FAVORITE_LOADER_ID, null, this);
        }
        super.onActivityCreated(savedInstanceState);
    }

    private static final String[] TRAILER_COLUMNS = new String[]{
            TrailerEntry.TABLE_NAME + "." + TrailerEntry._ID,
            TrailerEntry.COLUMN_NAME
    };

    // These indices are tied to TRAILER_COLUMNS.  If TRAILER_COLUMNS changes, these
    // must change.
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_NAME = 1;

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
            case FAVORITE_LOADER_ID:
                uri = MoviesContract.FavoriteEntry.CONTENT_URI;
                return new CursorLoader(getActivity(), uri, null,
                        FavoriteEntry.COLUMN_FAVORITE_ID + " = ?",
                        new String[]{Long.toString(ContentUris.parseId(mUri))},
                        null);
            default:
                return null;
        }
    }

    private boolean mStarState;
    private boolean mStarLocalState;

    private View.OnClickListener onStarClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v instanceof ImageButton) {
                mStarLocalState = !mStarLocalState;
                setStarImage(mStarLocalState);
                ContentResolver cr = getActivity().getContentResolver();
                if (mStarLocalState) {
                    ContentValues cv = new ContentValues();
                    cv.put(FavoriteEntry.COLUMN_FAVORITE_ID, ContentUris.parseId(mUri));
                    cr.insert(FavoriteEntry.CONTENT_URI, cv);
                } else {
                    cr.delete(FavoriteEntry.CONTENT_URI, FavoriteEntry.COLUMN_FAVORITE_ID + " = ?",
                            new String[]{Long.toString(ContentUris.parseId(mUri))});
                }
            }
        }
    };

    private void setStarImage(boolean isFavorite) {
        if (mButtonStar == null) {
            return;
        }
        if (isFavorite) {
            mButtonStar.setImageResource(R.drawable.ic_star_black_48dp);
        } else {
            mButtonStar.setImageResource(R.drawable.ic_star_border_black_48dp);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case TRAILERS_LOADER_ID:
                mTrailerAdapter.swapCursor(data);
                break;
            case DETAILS_LOADER_ID:
                View rootView = getListView();
                if (rootView == null || data == null) {
                    return;
                }
                if (!data.moveToFirst()) {
                    Log.d(LOG_TAG, "Empty cursor");
                    return;
                }
                String mDetailTitle = data.getString(COLUMN_TITLE);
                String mReleaseDate = format(Locale.getDefault(), "Release date:%n%s",
                        data.getString(COLUMN_RELEASE_DATE));
                String mDetailRating = String.format(Locale.getDefault(), "Rating:%n%1.1f/10",
                        data.getDouble(COLUMN_VOTE_AVERAGE));
                String mDetailOverview = data.getString(COLUMN_OVERVIEW);
                String mPosterPath = data.getString(COLUMN_POSTER_PATH);
                if (mHeaderView != null) {
                    ((TextView) mHeaderView.findViewById(R.id.detail_title)).setText(mDetailTitle);
                    ((TextView) mHeaderView.findViewById(R.id.detail_release_date)).setText(mReleaseDate);
                    ((TextView) mHeaderView.findViewById(R.id.detail_rating)).setText(mDetailRating);
                    ((TextView) mHeaderView.findViewById(R.id.detail_overview)).setText(mDetailOverview);
                    Picasso.with(getContext()).load(mPosterPath).into(
                            (ImageView) mHeaderView.findViewById(R.id.detail_poster)
                    );
                    mButtonStar = (ImageButton) mHeaderView.findViewById(R.id.mark_as_favorite_button);
                    mButtonStar.setOnClickListener(onStarClickListener);
                    setStarImage(mStarLocalState);
                }
                break;
            case FAVORITE_LOADER_ID:
                mStarState = mStarLocalState = data.moveToFirst();
                setStarImage(mStarState);
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
