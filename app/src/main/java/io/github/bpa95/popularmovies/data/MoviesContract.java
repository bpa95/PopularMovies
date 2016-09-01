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
        /**
         * Boolean column, true if movie is marked as favorite, false - otherwise
         */
        public static final String COLUMN_FAVORITE = "favorite";

        public static final String PARAM_SORT_BY = "sort_by";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final int SORT_BY_POPULARITY = 0;
        public static final int SORT_BY_TOP_RATED = 1;

        /**
         * Builds content uri with sort_by query parameter
         *
         * @param sortBy one of MovieEntry SORT_BY constants
         * @return content uri with appended query parameter if sortBy is one of SORT_BY constants,
         * just content uri - otherwise
         */
        public static Uri buildMovieSortedUri(int sortBy) {
            switch (sortBy) {
                case SORT_BY_POPULARITY:
                case SORT_BY_TOP_RATED:
                    return CONTENT_URI.buildUpon().appendQueryParameter(PARAM_SORT_BY, Integer.toString(sortBy)).build();
                default:
                    return CONTENT_URI;
            }
        }

        /**
         * Extracts sort_by query parameter from uri
         *
         * @param uri given uri
         * @return query parameter if it is one of SORT_BY constants,
         * SORT_BY_POPULARITY - otherwise
         */
        public static int getSortByFromUri(Uri uri) {
            String sortBy = uri.getQueryParameter(PARAM_SORT_BY);
            if (sortBy != null && sortBy.length() > 0) {
                int sortByInt = Integer.parseInt(sortBy);
                switch (sortByInt) {
                    case SORT_BY_POPULARITY:
                    case SORT_BY_TOP_RATED:
                        return sortByInt;
                    default:
                        return SORT_BY_POPULARITY;
                }
            } else {
                return SORT_BY_POPULARITY;
            }
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
        public static final String COLUMN_MOVIE_ID = "movie_id";
        /**
         * Url to the trailer
         */
        public static final String COLUMN_TRAILER_PATH = "trailer_path";

        public static Uri buildTrailerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTrailersByMovieUri(int movieId) {
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(movieId)).build();
        }

        public static int getMovieIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }
    }
}
