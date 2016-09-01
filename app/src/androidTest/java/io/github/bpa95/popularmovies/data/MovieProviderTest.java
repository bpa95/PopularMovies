package io.github.bpa95.popularmovies.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

public class MovieProviderTest extends AndroidTestCase {

    private static final int TEST_MOVIE_ID = 123;

    private static final Uri TEST_MOVIE = MoviesContract.MovieEntry.CONTENT_URI;
    private static final Uri TEST_TRAILER = MoviesContract.TrailerEntry.CONTENT_URI;
    private static final Uri TEST_TRAILER_BY_MOVIE = MoviesContract.TrailerEntry.buildTrailersByMovieUri(TEST_MOVIE_ID);

    public void testBuildUriMatcher() {
        UriMatcher matcher = MovieProvider.buildUriMatcher();

        assertEquals("Error: The MOVIE URI was matched incorrectly.",
                matcher.match(TEST_MOVIE), MovieProvider.MOVIE);
        assertEquals("Error: The TRAILER URI was matched incorrectly.",
                matcher.match(TEST_TRAILER), MovieProvider.TRAILER);
        assertEquals("Error: The MOVIE SORTED URI was matched incorrectly.",
                matcher.match(TEST_TRAILER_BY_MOVIE), MovieProvider.TRAILERS_BY_MOVIE);

    }

    public void testGetType() {
        String type = mContext.getContentResolver().getType(MoviesContract.MovieEntry.CONTENT_URI);
        assertEquals("Error: the MovieEntry CONTENT_URI shoud return MovieEntry.CONTENT_TYPE",
                type, MoviesContract.MovieEntry.CONTENT_TYPE);
    }
}