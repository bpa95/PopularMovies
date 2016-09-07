package io.github.bpa95.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.github.bpa95.popularmovies.data.MoviesContract.MovieEntry;

public class Movie implements Parcelable {
    int id;
    Uri posterPath;
    String title;
    String releaseDate;
    double popularity;
    double voteAverage;
    String overview;
    int favorite;

    Movie() {
    }

    Movie(JSONObject jsonMovie) throws JSONException {
        // These are the names of the JSON objects that need to be extracted
        final String TMDB_MOVIE_ID = "id";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_TITLE = "title";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_POPULARITY = "popularity";
        final String TMDB_VOTE_AVERAGE = "vote_average";
        final String TMDB_OVERVIEW = "overview";

        id = jsonMovie.getInt(TMDB_MOVIE_ID);
        posterPath = createPosterPath(jsonMovie.getString(TMDB_POSTER_PATH));
        title = jsonMovie.getString(TMDB_TITLE);
        releaseDate = parseDate(jsonMovie.getString(TMDB_RELEASE_DATE));
        popularity = jsonMovie.getDouble(TMDB_POPULARITY);
        voteAverage = jsonMovie.getDouble(TMDB_VOTE_AVERAGE);
        overview = jsonMovie.getString(TMDB_OVERVIEW);
        favorite = 0;
    }

    private Uri createPosterPath(String path) {
        final String BASE_URL = "http://image.tmdb.org/t/p/";
        final String IMAGE_SIZE = "w185";

        return Uri.parse(BASE_URL).buildUpon()
                .appendEncodedPath(IMAGE_SIZE)
                .appendEncodedPath(path)
                .build();
    }

    Movie(Cursor cursor) {
        id = cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID));
        posterPath = Uri.parse(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER_PATH)));
        title = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE));
        releaseDate = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE));
        popularity = cursor.getDouble(cursor.getColumnIndex(MovieEntry.COLUMN_POPULARITY));
        voteAverage = cursor.getDouble(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE));
        overview = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW));
        favorite = cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_FAVORITE));
    }


    private static final String DB_DATE_FORMAT = "yyyy-dd-MM";
    private static final SimpleDateFormat dbDateFormat = new SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault());
    private static final String UI_DATE_FORMAT = "MMM d, yyyy";
    private static final SimpleDateFormat uiDateFormat = new SimpleDateFormat(UI_DATE_FORMAT, Locale.getDefault());

    private static String parseDate(String date) {
        String pDate;
        try {
            pDate = uiDateFormat.format(dbDateFormat.parse(date));
        } catch (ParseException e) {
            pDate = "Unknown";
        }
        return pDate;
    }

    private Movie(Parcel in) {
        id = in.readInt();
        posterPath = in.readParcelable(Uri.class.getClassLoader());
        title = in.readString();
        releaseDate = in.readString();
        popularity = in.readDouble();
        voteAverage = in.readDouble();
        overview = in.readString();
        favorite = in.readInt();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeParcelable(posterPath, i);
        parcel.writeString(title);
        parcel.writeString(releaseDate);
        parcel.writeDouble(popularity);
        parcel.writeDouble(voteAverage);
        parcel.writeString(overview);
        parcel.writeInt(favorite);
    }
}
