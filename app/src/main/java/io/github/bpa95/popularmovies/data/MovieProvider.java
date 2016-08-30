package io.github.bpa95.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

public class MovieProvider extends ContentProvider {

    static final int MOVIE = 100;
    static final int MOVIE_SORTED = 101;
    static final int TRAILER = 200;
    static final int TRAILERS_BY_MOVIE = 201;


    static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, MoviesContract.PATH_MOVIE, MOVIE);
        uriMatcher.addURI(authority, MoviesContract.PATH_MOVIE + "/*", MOVIE_SORTED);
        uriMatcher.addURI(authority, MoviesContract.PATH_TRAILER, TRAILER);
        uriMatcher.addURI(authority, MoviesContract.PATH_TRAILER + "/#", TRAILERS_BY_MOVIE);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
