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

    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(MovieEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(TrailerEntry.CONTENT_URI, null, null);

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Movie table during delete", 0,
                cursor == null ? -1 : cursor.getCount());
        if (cursor != null) {
            cursor.close();
        }

        cursor = mContext.getContentResolver().query(
                TrailerEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Trailer table during delete", 0,
                cursor == null ? -1 : cursor.getCount());
        if (cursor != null) {
            cursor.close();
        }
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        deleteAllRecordsFromProvider();
    }

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

    public void testDeleteRecords() {
        testInsert();

        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, movieObserver);

        TestUtilities.TestContentObserver trailerObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(TrailerEntry.CONTENT_URI, true, trailerObserver);

        deleteAllRecordsFromProvider();

        movieObserver.waitForNotificationOrFail();
        trailerObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(movieObserver);
        mContext.getContentResolver().unregisterContentObserver(trailerObserver);
    }

    public void testUpdate() {
        ContentValues movieValues = TestDb.createFakeMovieFavoriteValues();

        Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, movieValues);
        long movieRowId = ContentUris.parseId(movieUri);

        assertTrue(movieRowId != -1);

        ContentValues updatedValues = TestDb.createFakeMovieNotFavoriteValues();

        Cursor movieCursor = mContext.getContentResolver().query(MovieEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        if (movieCursor != null) {
            movieCursor.registerContentObserver(tco);
        } else {
            fail("movieCursor is null");
        }

        int count = mContext.getContentResolver().update(
                MovieEntry.CONTENT_URI,
                updatedValues,
                MovieEntry._ID + " = ?",
                new String[]{Long.toString(movieRowId)}
        );
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        tco.waitForNotificationOrFail();

        movieCursor.unregisterContentObserver(tco);
        movieCursor.close();

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                MovieEntry._ID + " = ?",
                new String[]{Long.toString(movieRowId)},
                null
        );

        TestDb.validateCursor("testUpdate.  Error validating movie entry update.",
                cursor, updatedValues);

        if (cursor != null) {
            cursor.close();
        } else {
            fail("testUpdate. Cursor is null");
        }
    }
}