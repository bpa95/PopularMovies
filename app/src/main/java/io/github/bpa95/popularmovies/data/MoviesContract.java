package io.github.bpa95.popularmovies.data;

import android.provider.BaseColumns;

/**
 * Defines table and column names for the movies database.
 */
public class MoviesContract {

    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie";

        /** Movie id as returned by API */
        public static final String COLUMN_MOVIE_ID = "movie_id";
        /** Url to the poster */
        public static final String COLUMN_POSTER_PATH = "poster_path";
        /** Title of the movie */
        public static final String COLUMN_TITLE = "title";
        /** Movies release date, stored as long in milliseconds since the epoch */
        public static final String COLUMN_RELEASE_DATE = "release_date";
        /** Movies rating */
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        /** Synopsis of the movie */
        public static final String COLUMN_OVERVIEW = "overview";
    }

    public static final class TrailerEntry implements BaseColumns {
        public static final String TABLE_NAME = "trailer";

        /** Foreign key into the movie table */
        public static final String COLUMN_MOVIE_ID = "movie_id";
        /** Url to the trailer */
        public static final String COLUMN_TRAILER_PATH = "trailer_path";
    }
}
