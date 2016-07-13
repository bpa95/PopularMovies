package io.github.bpa95.popularmovies;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

public class Movie {
    int id;
    Uri posterPath;
    String title;
    String releaseDate;
    double voteAverage;
    String overview;

    public Movie(JSONObject jsonMovie) throws JSONException {
        // These are the names of the JSON objects that need to be extracted
        final String TMDB_MOVIE_ID = "id";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_TITLE = "title";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_VOTE_AVERAGE = "vote_average";
        final String TMDB_OVERVIEW = "overview";

        id = jsonMovie.getInt(TMDB_MOVIE_ID);
        title = jsonMovie.getString(TMDB_TITLE);
        releaseDate = jsonMovie.getString(TMDB_RELEASE_DATE);
        voteAverage = jsonMovie.getDouble(TMDB_VOTE_AVERAGE);
        overview = jsonMovie.getString(TMDB_OVERVIEW);

        final String BASE_URL = "http://image.tmdb.org/t/p/";
        final String IMAGE_SIZE = "w185";
        final String IMAGE_PATH = jsonMovie.getString(TMDB_POSTER_PATH);

        posterPath = Uri.parse(BASE_URL).buildUpon()
                .appendEncodedPath(IMAGE_SIZE)
                .appendEncodedPath(IMAGE_PATH)
                .build();
    }
}
