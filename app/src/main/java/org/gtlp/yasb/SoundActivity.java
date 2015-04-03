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
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

public class SoundActivity extends ActionBarActivity {

	public static final String YASB = "YASB";
	public static SoundPlayer soundPlayerInstance;
	public static WebView webView;
	public static SharedPreferences preferences;
	private InitHelper initHelper;
	private HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();
	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle actionBarDrawerToggle;

	public static void Log(String message) {
		if (BuildConfig.DEBUG)
			Log.d(YASB, message);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
	@Override
	public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
		super.onCreate(savedInstanceState, persistentState);
		onCreate(savedInstanceState);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.getBoolean("saved")) {
			return;
		}
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		new NetworkChecker(this).execute();
		soundPlayerInstance = new SoundPlayer(this);
		initUI();
	}

	private void initUI() {
		setContentView(R.layout.activity_sound);
		TextView lt = (TextView) findViewById(R.id.textView1);
		lt.setText(getText(R.string.text_loading).toString().replace("%x", "0").replace("%y", "0"));

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

		initHelper = new InitHelper(this);
		initHelper.execute();

		setListeners();
	}

	private void setListeners() {
		((ListView) findViewById(R.id.listView)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
					case 0:
						Intent intent = new Intent(SoundActivity.this.getApplicationContext(), SettingsActivity.class);
						SoundActivity.this.startActivity(intent);
						return;
					case 1:
						new AboutDialogFragment().show(getSupportFragmentManager(), "AboutDialogFragment");
				}
			}
		});

		findViewById(R.id.playButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				soundPlayerInstance.start();
			}
		});

		findViewById(R.id.pauseButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				soundPlayerInstance.pause();
			}
		});
		((SeekBar) findViewById(R.id.seekBar)).setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
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
	public void onConfigurationChanged(Configuration configuration) {
		super.onConfigurationChanged(configuration);
		actionBarDrawerToggle.onConfigurationChanged(configuration);
		initUI();
	}

	@Override
	protected void onDestroy() {
		if (initHelper != null && initHelper.getStatus() == AsyncTask.Status.RUNNING)
			initHelper.cancel(true);
		soundPlayerInstance.release();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if (drawerLayout.isDrawerOpen(Gravity.START)) {
			drawerLayout.closeDrawers();
			return;
		}
		super.onBackPressed();
	}

	@Override
	public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		actionBarDrawerToggle.syncState();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
	public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
		super.onRestoreInstanceState(savedInstanceState, persistentState);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
	public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
		super.onSaveInstanceState(outState, outPersistentState);
		saveInstance(outState);
	}

	private void saveInstance(Bundle outState) {
		outState.putBoolean("saved", true);
		outState.putInt("seekMax", ((SeekBar) findViewById(R.id.seekBar)).getMax());
		outState.putInt("seekProgress", ((SeekBar) findViewById(R.id.seekBar)).getProgress());
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
	protected void onPause() {
		if (initHelper != null && initHelper.getStatus() == AsyncTask.Status.RUNNING)
			initHelper.cancel(true);

		if (soundPlayerInstance.isPlaying()) {
			soundPlayerInstance.pause();
		}
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveInstance(outState);
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