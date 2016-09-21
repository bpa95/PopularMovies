package io.github.bpa95.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements MoviesGridFragment.Callback {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static boolean mTwoPaneMode;

    public static final String PREF_SORT_ORDER = "pref_sort_order";
    public static final int PREF_SORT_BY_POPULARITY = 0;
    public static final int PREF_SORT_BY_RATING = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        return true;
    }

    private void changeSortOrder(int sortOrder) {
        getPreferences(MODE_PRIVATE).edit()
                .putInt(PREF_SORT_ORDER, sortOrder)
                .apply();
        // TODO: update MoviesGridView
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
