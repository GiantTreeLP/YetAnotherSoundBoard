package org.gtlp.yasb;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.util.HashMap;

public class SoundActivity extends ActionBarActivity {

    protected static File soundsDir;
    protected static WebView webView;
    protected static SharedPreferences preferences;
    InitHelper initHelper;
    int orientation;
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.getBoolean("saved")) {
            return;
        }
        soundsDir = getExternalFilesDir("sounds");
        orientation = getRequestedOrientation();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_sound);
        TextView lt = (TextView) findViewById(R.id.textView1);
        lt.setText(getText(R.string.text_loading).toString().replace("%x", "0").replace("%y", "0"));
        if (!BuildConfig.DEBUG) {
            AdView adView = (AdView) this.findViewById(R.id.adView);
            AdRequest.Builder adRequest = new AdRequest.Builder();
            adRequest.addTestDevice("E31615C89229AEDC2A9763B4301C3196");
            adView.loadAd(adRequest.build());
        }
        webView = new WebView(getApplicationContext());
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.bar);
        setSupportActionBar(toolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        SoundPlayer.setInstance(new SoundPlayer(this));
        initHelper = new InitHelper(this);
        initHelper.execute();
        new NetworkChecker().execute();

        findViewById(R.id.playButton).setOnClickListener(v -> SoundPlayer.getInstance().start());

        findViewById(R.id.pauseButton).setOnClickListener(v -> SoundPlayer.getInstance().pause());
        ((SeekBar) findViewById(R.id.seekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean oldState;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && SoundPlayer.player != null) SoundPlayer.player.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (SoundPlayer.player != null) {
                    oldState = SoundPlayer.player.isPlaying();
                    SoundPlayer.player.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (oldState && SoundPlayer.player != null) SoundPlayer.player.start();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        actionBarDrawerToggle.syncState();
    }

    @Override
    protected void onDestroy() {
        if (initHelper != null) initHelper.cancel(true);
        SoundPlayer.getInstance().release();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (initHelper != null && initHelper.getStatus() == AsyncTask.Status.RUNNING)
            initHelper.cancel(true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putBoolean("saved", true);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("saved", true);
        super.onSaveInstanceState(outState);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sound, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) return true;
        int id = item.getItemId();
        switch (id) {
            case R.id.action_about:
                new AboutDialogFragment().show(getSupportFragmentManager(), "AboutDialogFragment");
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        actionBarDrawerToggle.onConfigurationChanged(configuration);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.setAppOptOut(preferences.getBoolean("opt_out", false));
            if (BuildConfig.DEBUG) {
                analytics.setDryRun(true);
                analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            }
            Tracker t = analytics.newTracker(R.xml.app_tracker);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }

    public enum TrackerName {
        APP_TRACKER
    }
}