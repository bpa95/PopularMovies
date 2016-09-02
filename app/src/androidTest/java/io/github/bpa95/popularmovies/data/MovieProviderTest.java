package io.github.bpa95.popularmovies.data;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

public class MovieProviderTest extends AndroidTestCase {

    private static final int TEST_MOVIE_ID = 123;

    private static final Uri TEST_MOVIE = MoviesContract.MovieEntry.CONTENT_URI;
    private static final Uri TEST_MOVIE_FAVORITE = MoviesContract.MovieEntry.buildMovieFavorite();
    private static final Uri TEST_TRAILER = MoviesContract.TrailerEntry.CONTENT_URI;
    private static final Uri TEST_TRAILER_BY_MOVIE = MoviesContract.TrailerEntry.buildTrailersByMovieIdUri(TEST_MOVIE_ID);

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    public void testBuildUriMatcher() {
        UriMatcher matcher = MovieProvider.buildUriMatcher();

        assertEquals("Error: The MOVIE URI was matched incorrectly.",
                matcher.match(TEST_MOVIE), MovieProvider.MOVIE);
        assertEquals("Error: The MOVIE FAVORITE URI was matched incorrectly.",
                matcher.match(TEST_MOVIE_FAVORITE), MovieProvider.MOVIE_FAVORITE);
        assertEquals("Error: The TRAILER URI was matched incorrectly.",
                matcher.match(TEST_TRAILER), MovieProvider.TRAILER);
        assertEquals("Error: The MOVIE SORTED URI was matched incorrectly.",
                matcher.match(TEST_TRAILER_BY_MOVIE), MovieProvider.TRAILERS_BY_MOVIE);

    }

    public void testGetType() {
        String type = mContext.getContentResolver().getType(MoviesContract.MovieEntry.CONTENT_URI);
        assertEquals("Error: the MovieEntry CONTENT_URI should return MovieEntry.CONTENT_TYPE",
                type, MoviesContract.MovieEntry.CONTENT_TYPE);
    }

    public void testMovieQuery() {
        SQLiteDatabase db = new MovieDbHelper(mContext).getWritableDatabase();
        ContentValues testFavoriteValues = TestDb.createFakeMovieFavoriteValues();
        long rowMovieId = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, testFavoriteValues);
        assertTrue("Unable to insert MovieEntry into the Database", rowMovieId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                MoviesContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        TestDb.validateCursor("", cursor, testFavoriteValues);

        ContentValues testNotFavoriteValues = TestDb.createFakeMovieNotFavoriteValues();
        long rowId = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, testNotFavoriteValues);
        assertTrue("Unable to insert MovieEntry into the Database", rowId != -1);

        cursor = mContext.getContentResolver().query(
                MoviesContract.MovieEntry.buildMovieFavorite(),
                null,
                null,
                null,
                null
        );
        TestDb.validateCursor("", cursor, testFavoriteValues);

        ContentValues testTrailerValues = TestDb.createFakeTrailerValues(rowMovieId);
        rowId = db.insert(MoviesContract.TrailerEntry.TABLE_NAME, null, testTrailerValues);
        assertTrue("Unable to insert TrailerEntry into the Database", rowId != -1);
        db.close();

        cursor = mContext.getContentResolver().query(
                MoviesContract.TrailerEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        TestDb.validateCursor("", cursor, testTrailerValues);

        cursor = mContext.getContentResolver().query(
                MoviesContract.TrailerEntry.buildTrailersByMovieIdUri(
                        testFavoriteValues.getAsInteger(MoviesContract.MovieEntry.COLUMN_MOVIE_ID)),
                null,
                null,
                null,
                null
        );
        TestDb.validateCursor("", cursor, testTrailerValues);
    }

}