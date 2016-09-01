package io.github.bpa95.popularmovies.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/*
    Students: This is NOT a complete test for the WeatherContract --- just for the functions
    that we expect you to write.
 */
public class TestMovieContract extends AndroidTestCase {

    // intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final int TEST_SORT_BY = MoviesContract.MovieEntry.SORT_BY_TOP_RATED;

    public void testBuildMovieSortedUri() {
        Uri uri = MoviesContract.MovieEntry.buildMovieSortedUri(TEST_SORT_BY);
        assertNotNull("Error: Null Uri returned.", uri);
        assertEquals("Error: SortBy not properly appended to the end of the Uri",
                TEST_SORT_BY, Integer.parseInt(uri.getQueryParameter(MoviesContract.MovieEntry.PARAM_SORT_BY)));
        assertEquals("Error: Uri doesn't match expected result",
                uri.toString(),
                "content://io.github.bpa95.popularmovies/movie?sort_by=1");
        assertEquals("Error: getSortByFromUri doesn't work",
                MoviesContract.MovieEntry.getSortByFromUri(uri), TEST_SORT_BY);
    }

    private static final int TEST_MOVIE_ID = 123;

    public void testBuildTrailersByMovieUri() {
        Uri uri = MoviesContract.TrailerEntry.buildTrailersByMovieUri(TEST_MOVIE_ID);
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
