package io.github.bpa95.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.github.bpa95.popularmovies.data.MoviesContract.MovieEntry;
import io.github.bpa95.popularmovies.data.MoviesContract.TrailerEntry;
import io.github.bpa95.popularmovies.data.MoviesContract.FavoriteEntry;
import io.github.bpa95.popularmovies.data.MoviesContract.ReviewEntry;

public class MovieDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 5;

    static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + "(" +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " INTEGER NOT NULL," +
                MovieEntry.COLUMN_POPULARITY + " REAL NOT NULL, " +
                MovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL,  " +
                MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL,  " +

                " UNIQUE (" + MovieEntry.COLUMN_MOVIE_ID + ") ON CONFLICT IGNORE);";

        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " + TrailerEntry.TABLE_NAME + " (" +
                TrailerEntry._ID + " INTEGER PRIMARY KEY, " +
                TrailerEntry.COLUMN_ID_MOVIE + " INTEGER NOT NULL, " +
                TrailerEntry.COLUMN_KEY + " TEXT NOT NULL, " +
                TrailerEntry.COLUMN_NAME + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + TrailerEntry.COLUMN_ID_MOVIE + ") REFERENCES " +
                MovieEntry.TABLE_NAME + "(" + MovieEntry._ID + ")" +
                " );";

        final String SQL_CREATE_REVIEW_TABLE = "CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +
                ReviewEntry._ID + " INTEGER PRIMARY KEY, " +
                ReviewEntry.COLUMN_ID_MOVIE + " INTEGER NOT NULL, " +
                ReviewEntry.COLUMN_ID + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_URL + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + ReviewEntry.COLUMN_ID_MOVIE + ") REFERENCES " +
                MovieEntry.TABLE_NAME + "(" + MovieEntry._ID + ")" +
                " );";

        final String SQL_CREATE_FAVORITE_TABLE = "CREATE TABLE " + FavoriteEntry.TABLE_NAME + " (" +
                FavoriteEntry._ID + " INTEGER PRIMARY KEY, " +
                FavoriteEntry.COLUMN_FAVORITE_ID + " INTEGER NOT NULL, " +

                " FOREIGN KEY (" + FavoriteEntry.COLUMN_FAVORITE_ID + ") REFERENCES " +
                FavoriteEntry.TABLE_NAME + "(" + MovieEntry._ID + ")" +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRAILER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEW_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrailerEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
