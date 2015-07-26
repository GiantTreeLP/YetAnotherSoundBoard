package org.gtlp.yasb;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.concurrent.atomic.AtomicReference;

import io.fabric.sdk.android.Fabric;

public class SoundApplication extends Application {

    protected static final Crashlytics crashlytics = new Crashlytics();
    protected static final String CLICK_IDENTIFIER = "lastClick";
    protected static final String LONG_CLICK_IDENTIFIER = "lastLongClick";
    protected static AtomicReference<SoundPlayer> soundPlayerInstance = new AtomicReference<>(new SoundPlayer());
    protected static GoogleAnalytics analytics;
    protected static Tracker tracker;
    static SharedPreferences preferences;

    public static void log(String message) {
        crashlytics.core.log(Log.DEBUG, SoundActivity.YASB, message);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Fabric.with(this, crashlytics);

        analytics = GoogleAnalytics.getInstance(this);
        analytics.enableAutoActivityReports(this);
        analytics.setLocalDispatchPeriod(1800);
        analytics.setAppOptOut(preferences.getBoolean("opt_out", false));
        tracker = analytics.newTracker("UA-26925696-3");
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);
    }
}
