package io.github.bpa95.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.bpa95.popularmovies.data.MoviesContract.MovieEntry;
import io.github.bpa95.popularmovies.data.MoviesContract.TrailerEntry;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

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

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(MovieEntry.TABLE_NAME);
        tableNameHashSet.add(TrailerEntry.TABLE_NAME);

        deleteTheDatabase();
        SQLiteDatabase db = new MovieDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both tables
        assertTrue("Error: Your database was created without both tables",
                tableNameHashSet.isEmpty());
        c.close();

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + MovieEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<>();
        locationColumnHashSet.add(MovieEntry._ID);
        locationColumnHashSet.add(MovieEntry.COLUMN_MOVIE_ID);
        locationColumnHashSet.add(MovieEntry.COLUMN_POSTER_PATH);
        locationColumnHashSet.add(MovieEntry.COLUMN_TITLE);
        locationColumnHashSet.add(MovieEntry.COLUMN_RELEASE_DATE);
        locationColumnHashSet.add(MovieEntry.COLUMN_POPULARITY);
        locationColumnHashSet.add(MovieEntry.COLUMN_VOTE_AVERAGE);
        locationColumnHashSet.add(MovieEntry.COLUMN_OVERVIEW);


        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required movie entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    public static ContentValues createFakeMovieFavoriteValues() {
        ContentValues cv = new ContentValues();
        cv.put(MovieEntry.COLUMN_MOVIE_ID, 123);
        cv.put(MovieEntry.COLUMN_POSTER_PATH, "https://bfox.files.wordpress.com/2014/11/interstellar-movie.jpg");
        cv.put(MovieEntry.COLUMN_TITLE, "Interstellar");
        cv.put(MovieEntry.COLUMN_RELEASE_DATE, 1414213562);
        cv.put(MovieEntry.COLUMN_POPULARITY, 9.9); // TODO check 10.0
        cv.put(MovieEntry.COLUMN_VOTE_AVERAGE, 9.3);
        cv.put(MovieEntry.COLUMN_OVERVIEW, "Awesome movie");
        return cv;
    }

    public static ContentValues createFakeMovieNotFavoriteValues() {
        ContentValues cv = new ContentValues();
        cv.put(MovieEntry.COLUMN_MOVIE_ID, 124);
        cv.put(MovieEntry.COLUMN_POSTER_PATH, "https://bfox.files.wordpress.com/2014/11/interstellar-movie.jpg");
        cv.put(MovieEntry.COLUMN_TITLE, "Not Interstellar");
        cv.put(MovieEntry.COLUMN_RELEASE_DATE, 314159265);
        cv.put(MovieEntry.COLUMN_POPULARITY, 1.2); // TODO check 10.0
        cv.put(MovieEntry.COLUMN_VOTE_AVERAGE, 1.3);
        cv.put(MovieEntry.COLUMN_OVERVIEW, "Not awesome movie");
        return cv;
    }

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    public void testMovieTable() {
        insertMovie();
    }

    public static ContentValues createFakeTrailerValues(long rowId) {
        ContentValues cv = new ContentValues();
        cv.put(TrailerEntry.COLUMN_ID_MOVIE, rowId);
        cv.put(TrailerEntry.COLUMN_TRAILER_PATH, "https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        return cv;
    }

    public void testTrailerTable() {
        long rowMovieId = insertMovie();
        SQLiteDatabase db = new MovieDbHelper(this.mContext).getWritableDatabase();
        ContentValues cv = createFakeTrailerValues(rowMovieId);
        long rowTrailerId = db.insert(TrailerEntry.TABLE_NAME, null, cv);
        assertTrue(rowTrailerId != -1);
        Cursor c = db.query(
                TrailerEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
        validateCursor("", c, cv);
        db.close();
    }

    public long insertMovie() {
        SQLiteDatabase db = new MovieDbHelper(this.mContext).getWritableDatabase();
        ContentValues cv = createFakeMovieFavoriteValues();
        long rowId = db.insert(MovieEntry.TABLE_NAME, null, cv);
        Cursor c = db.query(
                MovieEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
        validateCursor("", c, cv);
        db.close();
        return rowId;
    }
}