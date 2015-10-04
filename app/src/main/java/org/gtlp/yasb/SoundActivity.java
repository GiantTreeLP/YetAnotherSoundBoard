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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class SoundActivity extends AppCompatActivity {

    static SeekBar seekBar;
    static TextView timeText;
    static View playButton;
    static View pauseButton;
    static TextView current;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);
        pauseButton = findViewById(R.id.pauseButton);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        playButton = findViewById(R.id.playButton);
        timeText = (TextView) findViewById(R.id.timetext);
        current = (TextView) findViewById(R.id.current);
        if (SoundApplication.soundPlayerInstance != null) {
            SoundApplication.soundPlayerInstance.release();
        }
        SoundApplication.soundPlayerInstance = new SoundPlayer();
        initUI();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (SoundApplication.preferences.getBoolean(SoundApplication.PREFKEY_FIRSTRUN, true) || BuildConfig.DEBUG) {
            SpannableString message = new SpannableString(getText(R.string.dialog_data_msg));
            Linkify.addLinks(message, Linkify.WEB_URLS);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_data_title)
                    .setMessage(message)
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SoundApplication.preferences.edit().putBoolean(SoundApplication.PREFKEY_FIRSTRUN, false).commit();
                        }
                    }).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (SoundApplication.soundPlayerInstance != null) {
            SoundApplication.soundPlayerInstance.release();
            SoundApplication.soundPlayerInstance = null;
        }
    }

    private void initUI() {
        try {
            if (SoundApplication.preferences.getInt(SoundApplication.PREFKEY_VERSION_CODE, 0) < getPackageManager().getPackageInfo(getPackageName(), 0).versionCode) {
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
        seekBar.setEnabled(false);
        setListeners();
    }

    private void setListeners() {
        ((ListView) findViewById(R.id.listView)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        //new SettingsActivity();
                        startActivityIfNeeded(new Intent(getApplicationContext(), SettingsActivity.class), 0);
                        return;
                    case 1:
                        new AboutDialogFragment().show(getSupportFragmentManager(), "AboutDialogFragment");
                        return;
                    case 2:
                        startActivityIfNeeded(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/GiantTreeLP/YetAnotherSoundBoard/")), 0);
                        return;
                    default:
                }
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SoundApplication.soundPlayerInstance != null && !SoundApplication.soundPlayerInstance.isPlaying()) {
                    SoundApplication.soundPlayerInstance.start();
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SoundApplication.soundPlayerInstance != null && SoundApplication.soundPlayerInstance.isPlaying()) {
                    SoundApplication.soundPlayerInstance.pause();
                }
            }
        });
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            boolean oldState;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && SoundApplication.soundPlayerInstance != null)
                    SoundApplication.soundPlayerInstance.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (SoundApplication.soundPlayerInstance != null) {
                    oldState = SoundApplication.soundPlayerInstance.isPlaying();
                    SoundApplication.soundPlayerInstance.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (oldState && SoundApplication.soundPlayerInstance != null)
                    SoundApplication.soundPlayerInstance.start();
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
        if (SoundApplication.soundPlayerInstance != null) {
            if (SoundApplication.soundPlayerInstance.seeker != null && SoundApplication.soundPlayerInstance.seeker.getStatus().equals(AsyncTask.Status.RUNNING)) {
                SoundApplication.soundPlayerInstance.seeker.pause = true;
                SoundApplication.soundPlayerInstance.seeker.cancel(true);
            }
            if (SoundApplication.soundPlayerInstance.isPlaying()) {
                SoundApplication.soundPlayerInstance.pause();
            }

            SoundApplication.soundPlayerInstance.release();
            SoundApplication.soundPlayerInstance = null;
        }
    }

}