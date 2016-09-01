package io.github.bpa95.popularmovies.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/*
    Students: This is NOT a complete test for the WeatherContract --- just for the functions
    that we expect you to write.
 */
public class TestMovieContract extends AndroidTestCase {

    private static final int TEST_MOVIE_ID = 123;

    public void testBuildTrailersByMovieUri() {
        Uri uri = MoviesContract.TrailerEntry.buildTrailersByMovieIdUri(TEST_MOVIE_ID);
        assertNotNull("Error: Null Uri returned.", uri);
        assertEquals("Error: Movie id not properly appended to the end of the Uri",
                TEST_MOVIE_ID, Integer.parseInt(uri.getLastPathSegment()));
        assertEquals("Error: Uri doesn't match expected result",
                uri.toString(),
                "content://io.github.bpa95.popularmovies/trailer/123");
        assertEquals("Error: getMovieIdFromUri doesn't work",
                MoviesContract.TrailerEntry.getMovieIdFromUri(uri), TEST_MOVIE_ID);
    }

}
