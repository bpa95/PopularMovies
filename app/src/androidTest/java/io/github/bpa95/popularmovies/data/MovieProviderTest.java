package io.github.bpa95.popularmovies.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

import io.github.bpa95.popularmovies.data.MoviesContract.MovieEntry;
import io.github.bpa95.popularmovies.data.MoviesContract.TrailerEntry;

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

    public void deleteAllRecordsFromDB() {
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(MoviesContract.MovieEntry.TABLE_NAME, null, null);
        db.delete(MoviesContract.TrailerEntry.TABLE_NAME, null, null);
        db.close();
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        deleteAllRecordsFromDB();
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

    public void testQuery() {
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

    public void testInsert() {
        ContentValues movieValues = TestDb.createFakeMovieFavoriteValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, tco);
        Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, movieValues);

        // Did our content observer get called? If this fails, insert movie
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long movieRowId = ContentUris.parseId(movieUri);
        assertTrue(movieRowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        TestDb.validateCursor("Error validating MovieEntry", cursor, movieValues);

        ContentValues trailerValues = TestDb.createFakeTrailerValues(movieRowId);
        tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(TrailerEntry.CONTENT_URI, true, tco);
        Uri trailerUri = mContext.getContentResolver().insert(TrailerEntry.CONTENT_URI, trailerValues);
        assertTrue(trailerUri != null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);
        cursor = mContext.getContentResolver().query(
                TrailerEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        TestDb.validateCursor("Error validating TrailerEntry", cursor, trailerValues);

        // Add the trailer values in with the movie data so that we can make
        // sure that the join worked and we actually get all the values back
        movieValues.putAll(trailerValues);

        cursor = mContext.getContentResolver().query(
                TrailerEntry.buildTrailersByMovieIdUri(movieValues.getAsInteger(MovieEntry.COLUMN_MOVIE_ID)),
                null,
                null,
                null,
                null
        );
        TestDb.validateCursor("Error validating joined Movie and Trailer data", cursor, movieValues);
    }

}