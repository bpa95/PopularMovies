package io.github.bpa95.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.github.bpa95.popularmovies.data.MoviesContract.FavoriteEntry;
import io.github.bpa95.popularmovies.data.MoviesContract.MovieEntry;
import io.github.bpa95.popularmovies.data.MoviesContract.ReviewEntry;
import io.github.bpa95.popularmovies.data.MoviesContract.TrailerEntry;

public class MovieProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String LOG_TAG = MovieProvider.class.getSimpleName();
    private MovieDbHelper mOpenHelper;

    public static final int MOVIE = 100;
    public static final int MOVIE_ITEM = 102;
    public static final int TRAILER = 200;
    public static final int TRAILERS_BY_MOVIE = 201;
    public static final int FAVORITE = 300;
    public static final int REVIEW = 400;
    public static final int REVIEWS_BY_MOVIE = 401;

    private static final SQLiteQueryBuilder sTrailersByMovieQueryBuilder;
    private static final SQLiteQueryBuilder sReviewsByMovieQueryBuilder;
    private static final SQLiteQueryBuilder sFavoriteByMovieQueryBuilder;

    static {
        sTrailersByMovieQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //movie INNER JOIN trailer ON trailer.id_movie = movie._id
        sTrailersByMovieQueryBuilder.setTables(
                MovieEntry.TABLE_NAME + " INNER JOIN " +
                        TrailerEntry.TABLE_NAME +
                        " ON " + TrailerEntry.TABLE_NAME +
                        "." + TrailerEntry.COLUMN_ID_MOVIE +
                        " = " + MovieEntry.TABLE_NAME +
                        "." + MovieEntry._ID);

        sReviewsByMovieQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //movie INNER JOIN review ON review.id_movie = movie._id
        sTrailersByMovieQueryBuilder.setTables(
                MovieEntry.TABLE_NAME + " INNER JOIN " +
                        ReviewEntry.TABLE_NAME +
                        " ON " + ReviewEntry.TABLE_NAME +
                        "." + ReviewEntry.COLUMN_ID_MOVIE +
                        " = " + MovieEntry.TABLE_NAME +
                        "." + MovieEntry._ID);

        sFavoriteByMovieQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //movie INNER JOIN favorite ON favorite.favorite_id = movie._id
        sFavoriteByMovieQueryBuilder.setTables(
                MovieEntry.TABLE_NAME + " INNER JOIN " +
                        FavoriteEntry.TABLE_NAME +
                        " ON " + FavoriteEntry.TABLE_NAME +
                        "." + FavoriteEntry.COLUMN_FAVORITE_ID +
                        " = " + MovieEntry.TABLE_NAME +
                        "." + MovieEntry._ID);
    }


    static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, MoviesContract.PATH_MOVIE, MOVIE);
        uriMatcher.addURI(authority, MoviesContract.PATH_MOVIE + "/#", MOVIE_ITEM);
        uriMatcher.addURI(authority, MoviesContract.PATH_TRAILER, TRAILER);
        uriMatcher.addURI(authority, MoviesContract.PATH_TRAILER + "/#", TRAILERS_BY_MOVIE);
        uriMatcher.addURI(authority, MoviesContract.PATH_REVIEW, REVIEW);
        uriMatcher.addURI(authority, MoviesContract.PATH_REVIEW + "/#", REVIEWS_BY_MOVIE);
        uriMatcher.addURI(authority, MoviesContract.PATH_FAVORITE, FAVORITE);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case MOVIE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case MOVIE_ITEM:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieEntry.TABLE_NAME,
                        projection,
                        MovieEntry._ID + " = ?",
                        new String[]{Long.toString(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder
                );
                break;
            case TRAILER:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        TrailerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case TRAILERS_BY_MOVIE:
                retCursor = sTrailersByMovieQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        MovieEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{TrailerEntry.getMovieIdFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );
                break;
            case REVIEW:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ReviewEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case REVIEWS_BY_MOVIE:
                retCursor = sReviewsByMovieQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        MovieEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{ReviewEntry.getMovieIdFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );
                break;
            case FAVORITE:
                retCursor = sFavoriteByMovieQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
        Context context = getContext();
        if (retCursor != null && context != null) {
            retCursor.setNotificationUri(context.getContentResolver(), uri);
        }
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                return MovieEntry.CONTENT_TYPE;
            case MOVIE_ITEM:
                return MovieEntry.CONTENT_ITEM_TYPE;
            case TRAILER:
            case TRAILERS_BY_MOVIE:
                return TrailerEntry.CONTENT_TYPE;
            case REVIEW:
            case REVIEWS_BY_MOVIE:
                return ReviewEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                long _id = db.insert(MovieEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = MovieEntry.buildMovieUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case TRAILER: {
                long _id = db.insert(TrailerEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = TrailerEntry.buildTrailerUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case REVIEW: {
                long _id = db.insert(ReviewEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = ReviewEntry.buildReviewUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case FAVORITE: {
                long _id = db.insert(FavoriteEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = FavoriteEntry.buildFavoriteUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context context = getContext();
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case MOVIE: {
                rowsDeleted = db.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case TRAILER: {
                rowsDeleted = db.delete(TrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case REVIEW: {
                rowsDeleted = db.delete(ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context context = getContext();
        if (rowsDeleted > 0 && context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE: {
                rowsUpdated = db.update(MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case TRAILER: {
                rowsUpdated = db.update(TrailerEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case REVIEW: {
                rowsUpdated = db.update(ReviewEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context context = getContext();
        if (rowsUpdated > 0 && context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                Context context = getContext();
                if (context != null) {
                    context.getContentResolver().notifyChange(uri, null);
                }
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
