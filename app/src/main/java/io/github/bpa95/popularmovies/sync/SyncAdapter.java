package io.github.bpa95.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;

import io.github.bpa95.popularmovies.MainActivity;
import io.github.bpa95.popularmovies.objects.Movie;
import io.github.bpa95.popularmovies.R;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String LOG_TAG = SyncAdapter.class.getSimpleName();
    private static final long MINUTE = 60L;
    private static final long HOUR = 60 * MINUTE;
    private static final long SYNC_INTERVAL = 24 * HOUR;
//    private static final long SYNC_INTERVAL = 24 * HOUR;
    private static final long SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    ContentResolver mContentResolver;

    private static final String EXTRA_SORT_ORDER = "io.github.bpa95.popularmovies.sync.extra.SORT_ORDER";

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        try {
            Log.d(LOG_TAG, "Performing sync");
            Movie.transferMoviesFromServerToLocalDb(getContext(), provider);
        } catch (IOException | JSONException | RemoteException e) {
            Log.e(LOG_TAG, "Error ", e);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Log.d(LOG_TAG, "syncImmediately Called.");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        Account account = getSyncAccount(context);
        final String authority = context.getString(R.string.content_authority);
        bundle.putString(EXTRA_SORT_ORDER, getSortOrder(context));
        ContentResolver.requestSync(account, authority, bundle);
        Log.d(LOG_TAG, "syncImmediately finished.");
    }

    private static String getSortOrder(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int sortOrderPref = prefs.getInt(MainActivity.PREF_SORT_ORDER,
                MainActivity.PREF_SORT_BY_POPULARITY);
        String sortOrder = context.getString(R.string.pref_sortOrder_popular_value);
        if (sortOrderPref == MainActivity.PREF_SORT_BY_RATING) {
            sortOrder = context.getString(R.string.pref_sortOrder_topRated_value);
        }
        return sortOrder;
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

            /*
             * Add the account and account type, no password or user data
             * If successful, return the Account object, otherwise report an error.
             */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                Log.d(LOG_TAG, "getSyncAccount inside if if");
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, long syncInterval, long flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_SORT_ORDER, getSortOrder(context));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(bundle).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, bundle, syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        // Since we've created an account
        configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        // Without calling setSyncAutomatically, our periodic sync will not be enabled.
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        // Finally, let's do a sync to get things started
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
