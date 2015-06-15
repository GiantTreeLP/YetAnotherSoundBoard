package org.gtlp.yasb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.Tracker;

import java.util.concurrent.atomic.AtomicReference;

import io.fabric.sdk.android.Fabric;

public class SoundActivity extends AppCompatActivity {

    public static final String YASB = "YASB";
    public static final String PREFKEY_VERSION_CODE = "versionCode";
    protected static final AtomicReference<SoundPlayer> soundPlayerInstance = new AtomicReference<>();
    protected static int uniqueId = 0xF;
    protected static Tracker tracker;
    protected static TextView current;
    static SeekBar seekBar;
    static TextView timeText;
    static View playButton;
    static View pauseButton;
    static SharedPreferences preferences;
    private static Crashlytics crashlytics;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    public static void setUniqueId(int uniqueId) {
        SoundActivity.uniqueId = uniqueId;
    }

    public static void log(String message) {
        if (crashlytics != null && crashlytics.core != null) {
            crashlytics.core.log(Log.DEBUG, YASB, message);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Fabric.with(this, (crashlytics = new Crashlytics()));
        setContentView(R.layout.activity_sound);
        pauseButton = findViewById(R.id.pauseButton);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        playButton = findViewById(R.id.playButton);
        timeText = (TextView) findViewById(R.id.timetext);
        current = (TextView) findViewById(R.id.current);
        if (soundPlayerInstance.get() != null) {
            soundPlayerInstance.get().release();
        }
        soundPlayerInstance.set(new SoundPlayer());
        initUI();

        /*analytics = GoogleAnalytics.getInstance(this);
        analytics.enableAutoActivityReports(getApplication());
        analytics.setLocalDispatchPeriod(1800);
        analytics.setAppOptOut(preferences.getBoolean("opt_out", false));
        tracker = analytics.newTracker("UA-26925696-3");
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);*/
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (soundPlayerInstance.get() != null) {
            soundPlayerInstance.get().release();
            soundPlayerInstance.set(null);
        }
    }

    private void initUI() {
        try {
            if (preferences.getInt(PREFKEY_VERSION_CODE, 0) < getPackageManager().getPackageInfo(getPackageName(), 0).versionCode) {
                new ChangelogDialogFragment().show(getSupportFragmentManager(), "ChangelogDialogFragment");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        playButton.setEnabled(false);
        pauseButton.setEnabled(false);

        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest.Builder adRequest = new AdRequest.Builder();
        adRequest.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        adRequest.addTestDevice("E31615C89229AEDC2A9763B4301C3196");
        adView.loadAd(adRequest.build());
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.bar);
        setSupportActionBar(toolbar);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        setListeners();
    }

    private void setListeners() {
        ((ListView) findViewById(R.id.listView)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        startActivity(new Intent(getBaseContext(), SettingsActivity.class));
                        return;
                    case 1:
                        new AboutDialogFragment().show(getSupportFragmentManager(), "AboutDialogFragment");
                        return;
                    case 2:
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/GiantTreeLP/YetAnotherSoundBoard/")));
                        return;
                    default:
                        return;
                }
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (soundPlayerInstance.get() != null && !soundPlayerInstance.get().isPlaying()) {
                    soundPlayerInstance.get().start();
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (soundPlayerInstance.get() != null && soundPlayerInstance.get().isPlaying()) {
                    soundPlayerInstance.get().pause();
                }
            }
        });
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            boolean oldState;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && soundPlayerInstance.get() != null)
                    soundPlayerInstance.get().seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (soundPlayerInstance.get() != null) {
                    oldState = soundPlayerInstance.get().isPlaying();
                    soundPlayerInstance.get().pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (oldState && soundPlayerInstance.get() != null)
                    soundPlayerInstance.get().start();
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (soundPlayerInstance.get() != null) {
            if (soundPlayerInstance.get().seeker != null && soundPlayerInstance.get().seeker.getStatus().equals(AsyncTask.Status.RUNNING)) {
                soundPlayerInstance.get().seeker.pause = true;
                soundPlayerInstance.get().seeker.cancel(true);
            }
            if (soundPlayerInstance.get().isPlaying()) {
                soundPlayerInstance.get().pause();
            }

            soundPlayerInstance.get().release();
            soundPlayerInstance.set(null);
        }
    }

}