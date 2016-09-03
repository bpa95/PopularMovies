package io.github.bpa95.popularmovies;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Movie implements Parcelable {
    int id;
    Uri posterPath;
    String title;
    long releaseDate;
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
        title = jsonMovie.getString(TMDB_TITLE);
        releaseDate = parseDate(jsonMovie.getString(TMDB_RELEASE_DATE));
        popularity = jsonMovie.getDouble(TMDB_POPULARITY);
        voteAverage = jsonMovie.getDouble(TMDB_VOTE_AVERAGE);
        overview = jsonMovie.getString(TMDB_OVERVIEW);
        favorite = 0;

        final String BASE_URL = "http://image.tmdb.org/t/p/";
        final String IMAGE_SIZE = "w185";
        final String IMAGE_PATH = jsonMovie.getString(TMDB_POSTER_PATH);

        posterPath = Uri.parse(BASE_URL).buildUpon()
                .appendEncodedPath(IMAGE_SIZE)
                .appendEncodedPath(IMAGE_PATH)
                .build();
    }

    private long parseDate(String date) {
        return 0;
    }

    private Movie(Parcel in) {
        id = in.readInt();
        posterPath = in.readParcelable(Uri.class.getClassLoader());
        title = in.readString();
        releaseDate = in.readLong();
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
        parcel.writeLong(releaseDate);
        parcel.writeDouble(popularity);
        parcel.writeDouble(voteAverage);
        parcel.writeString(overview);
        parcel.writeInt(favorite);
    }
}
