package org.gtlp.yasb;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class SoundActivity extends AppCompatActivity {

    public static final String CALLER_KEY = "caller";
    private SeekBar seekBar;
    private TextView timeText;
    private View playButton;
    private View pauseButton;
    private TextView current;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    public SeekBar getSeekBar() {
        return seekBar;
    }

    public TextView getTimeText() {
        return timeText;
    }

    public View getPlayButton() {
        return playButton;
    }

    public View getPauseButton() {
        return pauseButton;
    }

    public TextView getCurrent() {
        return current;
    }

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().hasExtra(CALLER_KEY) && getIntent().getCharSequenceExtra(CALLER_KEY).toString().equals(SplashActivity.NAME)) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        setContentView(R.layout.activity_sound);
        pauseButton = findViewById(R.id.pauseButton);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        playButton = findViewById(R.id.playButton);
        timeText = (TextView) findViewById(R.id.timetext);
        current = (TextView) findViewById(R.id.current);
        if (SoundApplication.getSoundPlayerInstance() != null) {
            SoundApplication.getSoundPlayerInstance().release();
        } else {
            SoundApplication.setSoundPlayerInstance(new SoundPlayer(this));
        }
        initUI();
    }

    @Override
    public final void onStart() {
        super.onStart();
        if (SoundApplication.getPreferences().getBoolean(SoundApplication.PREFKEY_FIRSTRUN, true) || BuildConfig.DEBUG) {
            SpannableString message = new SpannableString(getText(R.string.dialog_data_msg));
            Linkify.addLinks(message, Linkify.WEB_URLS);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_data_title)
                    .setMessage(message)
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SoundApplication.getPreferences().edit().putBoolean(SoundApplication.PREFKEY_FIRSTRUN, false).commit();
                        }
                    }).show();
            try {
                if (SoundApplication.getPreferences().getInt(SoundApplication.PREFKEY_VERSION_CODE, 0) < getPackageManager().getPackageInfo(getPackageName(), 0).versionCode) {
                    new ChangelogDialogFragment().show(getSupportFragmentManager(), "ChangelogDialogFragment");
                }
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
    }

    @Override
    protected final void onStop() {
        super.onStop();
        if (SoundApplication.getSoundPlayerInstance() != null) {
            SoundApplication.getSoundPlayerInstance().release();
            SoundApplication.setSoundPlayerInstance(null);
        }
    }

    private void initUI() {
        getPlayButton().setEnabled(false);
        getPauseButton().setEnabled(false);
        getSeekBar().setEnabled(false);

        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest.Builder adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice("E31615C89229AEDC2A9763B4301C3196").addTestDevice("CE877D36AC47F29D483AC1D279071D9F").addTestDevice("61486F517CDF205893C2DAB54DC114BB");
        adView.loadAd(adRequest.build());
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        setSupportActionBar(toolbar);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        setListeners();
    }

    private void setListeners() {
        ((ListView) findViewById(R.id.listView)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        SoundActivity.this.startActivityIfNeeded(new Intent(getApplicationContext(), SettingsActivity.class), 0);
                        return;
                    case 1:
                        new AboutDialogFragment().show(SoundActivity.this.getSupportFragmentManager(), "AboutDialogFragment");
                        return;
                    case 2:
                        SoundActivity.this.startActivityIfNeeded(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/GiantTreeLP/YetAnotherSoundBoard/")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);
                        return;
                    default:
                }
            }
        });

        getPlayButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SoundApplication.getSoundPlayerInstance() != null && !SoundApplication.getSoundPlayerInstance().isPlaying()) {
                    SoundApplication.getSoundPlayerInstance().start();
                }
            }
        });

        getPauseButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SoundApplication.getSoundPlayerInstance() != null && SoundApplication.getSoundPlayerInstance().isPlaying()) {
                    SoundApplication.getSoundPlayerInstance().pause();
                }
            }
        });
        getSeekBar().setOnSeekBarChangeListener(new SeekBarSoundPlayerController());
    }

    @Override
    protected final void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        actionBarDrawerToggle.syncState();
    }

    @Override
    public final void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected final void onPause() {
        super.onPause();
        if (SoundApplication.getSoundPlayerInstance() != null) {
            if (SoundApplication.getSoundPlayerInstance().getSeeker() != null && SoundApplication.getSoundPlayerInstance().getSeeker().getStatus().equals(AsyncTask.Status.RUNNING)) {
                SoundApplication.getSoundPlayerInstance().getSeeker().setPause(true);
                SoundApplication.getSoundPlayerInstance().getSeeker().cancel(true);
            }
            if (SoundApplication.getSoundPlayerInstance().isPlaying()) {
                SoundApplication.getSoundPlayerInstance().pause();
            }
        }
    }

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    private class SeekBarSoundPlayerController implements OnSeekBarChangeListener {
        private boolean oldState;

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
            if (fromUser && SoundApplication.getSoundPlayerInstance() != null) {
                SoundApplication.getSoundPlayerInstance().seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            if (SoundApplication.getSoundPlayerInstance() != null) {
                oldState = SoundApplication.getSoundPlayerInstance().isPlaying();
                SoundApplication.getSoundPlayerInstance().pause();
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            if (oldState && SoundApplication.getSoundPlayerInstance() != null) {
                SoundApplication.getSoundPlayerInstance().start();
            }
        }
    }
}