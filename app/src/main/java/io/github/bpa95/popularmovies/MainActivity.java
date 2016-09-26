package io.github.bpa95.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.stetho.Stetho;

import io.github.bpa95.popularmovies.service.MovieIntentService;

public class MainActivity extends AppCompatActivity implements MoviesGridFragment.Callback {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static boolean mTwoPaneMode;

    public static final String PREF_SORT_ORDER = "pref_sort_order";
    public static final int PREF_SORT_BY_POPULARITY = 0;
    public static final int PREF_SORT_BY_RATING = 1;

    public static final String PREF_FAVORITE = "pref_favorite";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO remove before release
        // Stetho.initializeWithDefaults(this);
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                .build()
        );

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        mTwoPaneMode = null != findViewById(R.id.movie_detail_container);
        if (mTwoPaneMode && savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, new DetailFragment())
                    .commit();
        }
    }

    @Override
    public void onMovieSelected(Uri uri) {
        if (mTwoPaneMode) {
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, uri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment)
                    .commit();
        } else {
            Intent intentDetail = new Intent(this, DetailActivity.class);
            intentDetail.setData(uri);
            startActivity(intentDetail);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.action_favorite);
        setIcon(item, getPreferences(MODE_PRIVATE).getBoolean(PREF_FAVORITE, false));
        return true;
    }

    private void changeSortOrder(int sortOrder) {
        getPreferences(MODE_PRIVATE).edit()
                .putInt(PREF_SORT_ORDER, sortOrder)
                .apply();
        updateGrid();
    }

    private void updateGrid() {
        ((MoviesGridFragment) getFragmentManager().findFragmentById(R.id.movies_grid_fragment))
                .updateGrid();
    }

    private void changeFavoriteState(MenuItem item) {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        boolean isFavorite = prefs.getBoolean(PREF_FAVORITE, false);
        if (isFavorite) {
            prefs.edit().putBoolean(PREF_FAVORITE, false).apply();
            setIcon(item, false);
        } else {
            prefs.edit().putBoolean(PREF_FAVORITE, true).apply();
            setIcon(item, true);
        }
        updateGrid();
    }

    private void setIcon(MenuItem item, boolean isFavorite) {
        if (isFavorite) {
            item.setIcon(R.drawable.ic_star_white_24dp);
        } else {
            item.setIcon(R.drawable.ic_star_border_white_24dp);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_sort_popularity:
                changeSortOrder(PREF_SORT_BY_POPULARITY);
                return true;
            case R.id.menu_sort_top_rated:
                changeSortOrder(PREF_SORT_BY_RATING);
                return true;
            case R.id.action_favorite:
                changeFavoriteState(item);
                return true;
            case R.id.action_refresh:
                SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
                int sortOrderPref = prefs.getInt(PREF_SORT_ORDER, PREF_SORT_BY_POPULARITY);
                String sortOrder = getString(R.string.pref_sortOrder_popular_value);
                if (sortOrderPref == PREF_SORT_BY_RATING) {
                    sortOrder = getString(R.string.pref_sortOrder_topRated_value);
                }
                MovieIntentService.loadMovies(this, sortOrder);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
