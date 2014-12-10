package org.gtlp.yasb;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
    InitHelper ih;
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        soundsDir = getExternalFilesDir("sounds");
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
        SoundPlayer.setInstance(new SoundPlayer(this));
        ih = new InitHelper(this);
        ih.execute();
        new NetworkChecker().execute();

        findViewById(R.id.playButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundPlayer.getInstance().start();
            }
        });

        findViewById(R.id.pauseButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundPlayer.getInstance().pause();
            }
        });
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

    @Override
    protected void onDestroy() {
        ih.cancel(true);
        SoundPlayer.getInstance().release();
        super.onDestroy();
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
        int id = item.getItemId();
        switch (id) {
            case R.id.action_about:
                new AboutDialogFragment().show(getFragmentManager(), "AboutDialogFragment");
        }
        return super.onOptionsItemSelected(item);
    }

    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
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