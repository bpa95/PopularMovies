package io.github.bpa95.popularmovies;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.RemoteException;

import org.json.JSONException;
import org.json.JSONObject;

import io.github.bpa95.popularmovies.data.MoviesContract.TrailerEntry;

public class Trailer {
    public int id_movie;
    public String key;
    public String name;

    public Trailer(JSONObject jsonTrailer, int id_movie) throws JSONException {
        final String TMDB_KEY = "key";
        final String TMDB_NAME = "name";

        this.id_movie = id_movie;
        key = jsonTrailer.getString(TMDB_KEY);
        name = jsonTrailer.getString(TMDB_NAME);
    }

    private final String[] projection = new String[]{TrailerEntry.COLUMN_KEY};
    private final String selection = TrailerEntry.COLUMN_KEY + " = ?";
    private final String[] selectionArgs = new String[]{key};

    public void insertTrailerInDb(ContentProviderClient provider) throws RemoteException {
        ContentValues cv = new ContentValues();
        cv.put(TrailerEntry.COLUMN_ID_MOVIE, id_movie);
        cv.put(TrailerEntry.COLUMN_KEY, key);
        cv.put(TrailerEntry.COLUMN_NAME, name);

        selectionArgs[0] = cv.getAsString(TrailerEntry.COLUMN_KEY);
        Cursor cursor = provider.query(
                TrailerEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
        if (cursor == null) {
            return;
        }
        if (!cursor.moveToFirst()) {
            provider.insert(TrailerEntry.CONTENT_URI, cv);
        } else {
            provider.update(
                    TrailerEntry.CONTENT_URI,
                    cv,
                    selection,
                    selectionArgs
            );
        }
        cursor.close();
    }
}
