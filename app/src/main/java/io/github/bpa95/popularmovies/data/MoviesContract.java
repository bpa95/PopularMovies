package io.github.bpa95.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the movies database.
 */
public class MoviesContract {

    public static final String CONTENT_AUTHORITY = "io.github.bpa95.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";
    public static final String PATH_TRAILER = "trailer";
    public static final String PATH_FAVORITE = "favorite";


    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String TABLE_NAME = "movie";

        /**
         * Movie id as returned by API
         */
        public static final String COLUMN_MOVIE_ID = "movie_id";
        /**
         * Url to the poster
         */
        public static final String COLUMN_POSTER_PATH = "poster_path";
        /**
         * Title of the movie
         */
        public static final String COLUMN_TITLE = "title";
        /**
         * Movies release date, stored as long in milliseconds since the epoch
         */
        public static final String COLUMN_RELEASE_DATE = "release_date";
        /**
         * Movies popularity
         */
        public static final String COLUMN_POPULARITY = "popularity";
        /**
         * Movies rating
         */
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        /**
         * Synopsis of the movie
         */
        public static final String COLUMN_OVERVIEW = "overview";

        public static final String PARAM_SORT_BY = "sort_by";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class TrailerEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;

        public static final String TABLE_NAME = "trailer";

        /**
         * Foreign key into the movie table
         */
        public static final String COLUMN_ID_MOVIE = "id_movie";
        /**
         * Trailer key
         */
        public static final String COLUMN_KEY = "key";
        /**
         * Trailer name
         */
        public static final String COLUMN_NAME = "name";

        public static Uri buildTrailerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTrailersByMovieIdUri(int movieId) {
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(movieId)).build();
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class FavoriteEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;

        public static final String TABLE_NAME = "favorite";

        /**
         * Foreign key into the movie table
         */
        public static final String COLUMN_FAVORITE_ID = "favorite_id";

        public static Uri buildFavoriteUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
