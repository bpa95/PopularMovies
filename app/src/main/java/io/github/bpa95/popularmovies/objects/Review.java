package io.github.bpa95.popularmovies.objects;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.RemoteException;

import org.json.JSONException;
import org.json.JSONObject;

import io.github.bpa95.popularmovies.data.MoviesContract.ReviewEntry;

public class Review {
    public int id_movie;
    public String id;
    public String author;
    public String content;
    public String url;

    public Review(JSONObject jsonReview, int id_movie) throws JSONException {
        final String TMDB_ID = "id";
        final String TMDB_AUTHOR = "author";
        final String TMDB_CONTENT = "content";
        final String TMDB_URL = "url";

        this.id_movie = id_movie;
        id = jsonReview.getString(TMDB_ID);
        author = jsonReview.getString(TMDB_AUTHOR);
        content = jsonReview.getString(TMDB_CONTENT);
        url = jsonReview.getString(TMDB_URL);
    }

    private final String[] projection = new String[]{ReviewEntry.COLUMN_ID};
    private final String selection = ReviewEntry.COLUMN_ID + " = ?";
    private final String[] selectionArgs = new String[]{id};

    public void insertReviewInDb(ContentProviderClient provider) throws RemoteException {
        ContentValues cv = new ContentValues();
        cv.put(ReviewEntry.COLUMN_ID_MOVIE, id_movie);
        cv.put(ReviewEntry.COLUMN_ID, id);
        cv.put(ReviewEntry.COLUMN_AUTHOR, author);
        cv.put(ReviewEntry.COLUMN_CONTENT, content);
        cv.put(ReviewEntry.COLUMN_URL, url);

        selectionArgs[0] = cv.getAsString(ReviewEntry.COLUMN_ID);
        Cursor cursor = provider.query(
                ReviewEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
        if (cursor == null) {
            return;
        }
        if (!cursor.moveToFirst()) {
            provider.insert(ReviewEntry.CONTENT_URI, cv);
        } else {
            provider.update(
                    ReviewEntry.CONTENT_URI,
                    cv,
                    selection,
                    selectionArgs
            );
        }
        cursor.close();
    }
}
