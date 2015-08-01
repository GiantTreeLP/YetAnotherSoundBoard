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
import com.google.android.gms.analytics.Tracker;

public class SoundActivity extends AppCompatActivity {

    protected static Tracker tracker;
    protected static TextView current;
    static SeekBar seekBar;
    static TextView timeText;
    static View playButton;
    static View pauseButton;
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
        if (SoundApplication.soundPlayerInstance.get() != null) {
            SoundApplication.soundPlayerInstance.get().release();
        }
        SoundApplication.soundPlayerInstance.set(new SoundPlayer());
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
        if (SoundApplication.soundPlayerInstance.get() != null) {
            SoundApplication.soundPlayerInstance.get().release();
            SoundApplication.soundPlayerInstance.set(null);
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
                if (SoundApplication.soundPlayerInstance.get() != null && !SoundApplication.soundPlayerInstance.get().isPlaying()) {
                    SoundApplication.soundPlayerInstance.get().start();
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SoundApplication.soundPlayerInstance.get() != null && SoundApplication.soundPlayerInstance.get().isPlaying()) {
                    SoundApplication.soundPlayerInstance.get().pause();
                }
            }
        });
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            boolean oldState;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && SoundApplication.soundPlayerInstance.get() != null)
                    SoundApplication.soundPlayerInstance.get().seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (SoundApplication.soundPlayerInstance.get() != null) {
                    oldState = SoundApplication.soundPlayerInstance.get().isPlaying();
                    SoundApplication.soundPlayerInstance.get().pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (oldState && SoundApplication.soundPlayerInstance.get() != null)
                    SoundApplication.soundPlayerInstance.get().start();
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
        if (SoundApplication.soundPlayerInstance.get() != null) {
            if (SoundApplication.soundPlayerInstance.get().seeker != null && SoundApplication.soundPlayerInstance.get().seeker.getStatus().equals(AsyncTask.Status.RUNNING)) {
                SoundApplication.soundPlayerInstance.get().seeker.pause = true;
                SoundApplication.soundPlayerInstance.get().seeker.cancel(true);
            }
            if (SoundApplication.soundPlayerInstance.get().isPlaying()) {
                SoundApplication.soundPlayerInstance.get().pause();
            }

            SoundApplication.soundPlayerInstance.get().release();
            SoundApplication.soundPlayerInstance.set(null);
        }
    }

}