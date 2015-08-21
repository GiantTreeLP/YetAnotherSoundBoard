package org.gtlp.yasb;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import io.fabric.sdk.android.Fabric;

class SoundApplication extends Application {

    public static final String PREFKEY_VERSION_CODE = "versionCode";
    public static final String PREFKEY_FIRSTRUN = "isFirstRun";
    static final Crashlytics crashlytics = new Crashlytics();
    static final String CLICK_IDENTIFIER = "lastClick";
    static final String LONG_CLICK_IDENTIFIER = "lastLongClick";
    private static final String YASB = "YASB";
    static SoundPlayer soundPlayerInstance = new SoundPlayer();
    static Tracker tracker;
    static SharedPreferences preferences;

    public static void log(String message) {
        crashlytics.core.log(Log.DEBUG, YASB, message);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Fabric.with(this, crashlytics);

        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        analytics.enableAutoActivityReports(this);
        analytics.setLocalDispatchPeriod(300);
        analytics.setAppOptOut(preferences.getBoolean("opt_out", false) || BuildConfig.DEBUG);
        tracker = analytics.newTracker("UA-26925696-3");
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);
    }
}
