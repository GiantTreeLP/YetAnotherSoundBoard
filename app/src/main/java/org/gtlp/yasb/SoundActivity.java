package org.gtlp.yasb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
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

import java.util.ArrayList;
import java.util.HashMap;

public class SoundActivity extends ActionBarActivity {

	public static final String YASB = "YASB";
	public static final String PREFKEY_VERSION_CODE = "versionCode";
	private static final String KEY_SAVED = "saved";
	private static final String KEY_SEEK_MAX = "seekMax";
	private static final String KEY_SEEK_PROGRESS = "seekProgress";
	private static final String KEY_FILE_INFOS = "fileInfos";
	public static SoundPlayer soundPlayerInstance;
	public static ArrayList<FileInfo> fileInfoArrayList;
	public static WebView webView;
	public static SharedPreferences preferences;
	private InitHelper initHelper;
	private HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();
	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle actionBarDrawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		restoreInstance(savedInstanceState);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		new NetworkChecker(this).execute();
		soundPlayerInstance = new SoundPlayer(this);
		initUI();
	}

	private void restoreInstance(Bundle savedInstanceState) {
		initHelper = new InitHelper(this);
		if (savedInstanceState != null && savedInstanceState.getBoolean(KEY_SAVED)) {
			((SeekBar) findViewById(R.id.seekBar)).setMax(savedInstanceState.getInt(KEY_SEEK_MAX));
			((SeekBar) findViewById(R.id.seekBar)).setProgress(savedInstanceState.getInt(KEY_SEEK_PROGRESS));
			initHelper.fileInfos = savedInstanceState.getParcelableArrayList(KEY_FILE_INFOS);
			initHelper.fileInfoSupplied = true;
			Log("seekMax: " + savedInstanceState.getInt(KEY_SEEK_MAX));
			Log("seekProgress: " + savedInstanceState.getInt(KEY_SEEK_PROGRESS));
			Log("Restored instance");
		} else if (fileInfoArrayList != null) {
			initHelper.fileInfos = fileInfoArrayList;
			initHelper.fileInfoSupplied = true;
		}
		initHelper.execute();
	}

	private void initUI() {
		setContentView(R.layout.activity_sound);
		try {
			if (preferences.getInt(PREFKEY_VERSION_CODE, 0) < getPackageManager().getPackageInfo(getPackageName(), 0).versionCode || BuildConfig.DEBUG) {
				new ChangelogDialogFragment().show(getSupportFragmentManager(), "ChangelogDialogFragment");
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
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

		setListeners();
	}

	public static void Log(String message) {
		if (BuildConfig.DEBUG)
			Log.d(YASB, message);
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
		if (initHelper == null) {
			initHelper = new InitHelper(this);
			initHelper.execute();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration configuration) {
		super.onConfigurationChanged(configuration);
		actionBarDrawerToggle.onConfigurationChanged(configuration);
		restoreInstance(null);
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
	public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
		super.onSaveInstanceState(outState, outPersistentState);
		saveInstance(outState);
	}

	private void saveInstance(Bundle outState) {
		if (initHelper != null && initHelper.getStatus() == AsyncTask.Status.FINISHED) {
			outState.putBoolean(KEY_SAVED, true);
			outState.putInt(KEY_SEEK_MAX, ((SeekBar) findViewById(R.id.seekBar)).getMax());
			outState.putInt(KEY_SEEK_PROGRESS, ((SeekBar) findViewById(R.id.seekBar)).getProgress());
			outState.putParcelableArrayList(KEY_FILE_INFOS, initHelper.fileInfos);
			fileInfoArrayList = initHelper.fileInfos;
			Log("Saved instance");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (initHelper != null && initHelper.getStatus() == AsyncTask.Status.RUNNING)
			initHelper.cancel(true);

		if (soundPlayerInstance.isPlaying()) {
			soundPlayerInstance.pause();
		}
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