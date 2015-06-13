package org.gtlp.yasb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class SoundActivity extends AppCompatActivity {

    public static final String YASB = "YASB";
    public static final String PREFKEY_VERSION_CODE = "versionCode";
    private static final String KEY_SAVED = "saved";
    private static final String KEY_SEEK_MAX = "seekMax";
    private static final String KEY_SEEK_PROGRESS = "seekProgress";
    private static final String KEY_FILE_INFOS = "fileInfos";
    public static WebView webView;
    public static volatile int uniqueId = 0xF;
    protected static GoogleAnalytics analytics;
    protected static volatile SoundPlayer soundPlayerInstance;
    protected static Tracker tracker;
    protected static SeekBar seekBar;
    protected static TextView timeText;
    protected static TextView current;
    protected static View playButton;
    protected static View pauseButton;
    static List<FileInfo> fileInfoArrayList;
    static SharedPreferences preferences;
    private InitHelper initHelper;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    public static void log(String message) {
        Crashlytics.log(Log.DEBUG, YASB, message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_sound);
        pauseButton = findViewById(R.id.pauseButton);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        playButton = findViewById(R.id.playButton);
        timeText = (TextView) findViewById(R.id.timetext);
        current = (TextView) findViewById(R.id.current);
        if (soundPlayerInstance != null) {
            soundPlayerInstance.release();
        }
        soundPlayerInstance = new SoundPlayer();
        initUI();
        restoreInstance(savedInstanceState);

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
        if (soundPlayerInstance != null) {
            soundPlayerInstance.release();
            soundPlayerInstance = null;
        }
    }

    private void restoreInstance(Bundle savedInstanceState) {
        initHelper = new InitHelper(this);
        if (savedInstanceState != null && savedInstanceState.getBoolean(KEY_SAVED)) {
            if (seekBar != null) {
                seekBar.setMax(savedInstanceState.getInt(KEY_SEEK_MAX));
                seekBar.setProgress(savedInstanceState.getInt(KEY_SEEK_PROGRESS));
            }
            initHelper.fileInfos = savedInstanceState.getParcelableArrayList(KEY_FILE_INFOS);
            initHelper.fileInfoSupplied = true;
            log("seekMax: " + savedInstanceState.getInt(KEY_SEEK_MAX));
            log("seekProgress: " + savedInstanceState.getInt(KEY_SEEK_PROGRESS));
            log("Restored instance");
        } else if (fileInfoArrayList != null) {
            initHelper.fileInfos = fileInfoArrayList;
            initHelper.fileInfoSupplied = true;
        }
        initHelper.execute();
    }

    private void initUI() {
        try {
            if (preferences.getInt(PREFKEY_VERSION_CODE, 0) < getPackageManager().getPackageInfo(getPackageName(), 0).versionCode) {
                new ChangelogDialogFragment().show(getSupportFragmentManager(), "ChangelogDialogFragment");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        TextView lt = (TextView) findViewById(R.id.textProgress);
        lt.setText(getText(R.string.text_loading).toString().replace("%x", "0").replace("%y", "0"));
        playButton.setEnabled(false);
        pauseButton.setEnabled(false);

        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest.Builder adRequest = new AdRequest.Builder();
        adRequest.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        adRequest.addTestDevice("E31615C89229AEDC2A9763B4301C3196");
        adView.loadAd(adRequest.build());

        webView = new WebView(getApplicationContext());
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
                if (soundPlayerInstance != null && !soundPlayerInstance.isPlaying()) {
                    soundPlayerInstance.start();
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (soundPlayerInstance != null && soundPlayerInstance.isPlaying()) {
                    soundPlayerInstance.pause();
                }
            }
        });
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            boolean oldState;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && soundPlayerInstance != null)
                    soundPlayerInstance.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (soundPlayerInstance != null) {
                    oldState = soundPlayerInstance.isPlaying();
                    soundPlayerInstance.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (oldState && soundPlayerInstance != null)
                    soundPlayerInstance.start();
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
    protected void onDestroy() {
        if (initHelper != null && initHelper.getStatus() == AsyncTask.Status.RUNNING)
            initHelper.cancel(true);
        super.onDestroy();
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
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        saveInstance(outState);
    }

    private void saveInstance(Bundle outState) {
        if (initHelper != null && initHelper.getStatus() == AsyncTask.Status.FINISHED) {
            outState.putBoolean(KEY_SAVED, true);
            outState.putInt(KEY_SEEK_MAX, seekBar.getMax());
            outState.putInt(KEY_SEEK_PROGRESS, seekBar.getProgress());
            outState.putParcelableArrayList(KEY_FILE_INFOS, new ArrayList<>(initHelper.fileInfos));
            fileInfoArrayList = initHelper.fileInfos;
            log("Saved instance");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (initHelper != null && initHelper.getStatus().equals(AsyncTask.Status.RUNNING)) {
            initHelper.cancel(true);
        }
        if (soundPlayerInstance != null) {
            if (soundPlayerInstance.isPlaying()) {
                soundPlayerInstance.pause();
            }
            if (soundPlayerInstance.seeker != null && soundPlayerInstance.seeker.getStatus().equals(AsyncTask.Status.RUNNING)) {
                soundPlayerInstance.seeker.pause = true;
                soundPlayerInstance.seeker.cancel(true);
            }

            soundPlayerInstance.release();
            soundPlayerInstance = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveInstance(outState);
    }

}