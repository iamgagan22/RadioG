package org.ggn.radioG;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import org.ggn.radioG.helpers.ConstantKeys;
import org.ggn.radioG.helpers.LogHelper;
import org.ggn.radioG.helpers.StorageHelper;

import java.io.File;


/**
 * MainActivity class
 */
public final class MainActivity extends AppCompatActivity {

    /* Define log tag */
    private static final String LOG_TAG = MainActivity.class.getSimpleName();


    /* Main class variables */
    private boolean mTwoPane;
    private File mCollectionFolder;
    private View mContainer;
    private BroadcastReceiver mCollectionChangedReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get collection folder
        StorageHelper storageHelper = new StorageHelper(this);
        mCollectionFolder = storageHelper.getCollectionDirectory();

        // set layout
        setContentView(R.layout.activity_main);

        // initialize broadcast receivers
        initializeBroadcastReceivers();
    }


    @Override
    protected void onResume() {
        super.onResume();

        // check if two pane mode can be used
        mTwoPane = detectTwoPane();

        // tablet mode: show player fragment in player container
        if (mTwoPane && mCollectionFolder.listFiles().length > 1) {
            // hide right pane
            mContainer.setVisibility(View.VISIBLE);
        } else if (mTwoPane) {
            // make room for action call
            mContainer.setVisibility(View.GONE);
        }

        saveAppState(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcastReceivers();
    }


    /*@Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        // check if two pane mode can be used
        mTwoPane = detectTwoPane();
    }*/


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // activity opened for second time set intent to new intent
        setIntent(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }


    // Workaround for an IllegalStateException crash (Fragment not attached to Activity)
    // See: https://github.com/y20k/transistor/issues/21
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment_main);
        // hand results over to fragment main
        fragment.onActivityResult(requestCode, resultCode, data);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment_main);
        // hand results over to fragment main
        fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    /* Checks if two-pane mode can be used */
    private boolean detectTwoPane() {
        mContainer = findViewById(R.id.player_container);

        // if player_container is present two-pane layout can be used
        if (mContainer != null) {
            LogHelper.v(LOG_TAG, "Large screen detected. Choosing two pane layout.");
            return true;
        } else {
            LogHelper.v(LOG_TAG, "Small screen detected. Choosing single pane layout.");
            return false;
        }
    }


    /* Saves app state to SharedPreferences */
    private void saveAppState(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        // editor.putInt(PREF_STATION_ID_SELECTED, mStationID);
        editor.putBoolean(ConstantKeys.PREF_TWO_PANE, mTwoPane);
        editor.apply();
        LogHelper.v(LOG_TAG, "Saving state. Two Pane = " + mTwoPane);
    }


    /* Unregisters broadcast receivers */
    private void unregisterBroadcastReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCollectionChangedReceiver);
    }


    /* Initializes broadcast receivers for onCreate */
    private void initializeBroadcastReceivers() {
        // RECEIVER: station added, deleted, or changed
        mCollectionChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // show/hide player layout container
                if (mTwoPane && mCollectionFolder.listFiles().length == 1) {
                    // make room for action call - hide player container
                    mContainer.setVisibility(View.GONE);
                } else if (mTwoPane) {
                    // show player container
                    mContainer.setVisibility(View.VISIBLE);
                }
            }
        };
        IntentFilter collectionChangedIntentFilter = new IntentFilter(ConstantKeys.ACTION_COLLECTION_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mCollectionChangedReceiver, collectionChangedIntentFilter);
    }

}