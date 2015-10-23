package org.gtlp.yasb;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import io.fabric.sdk.android.Fabric;

public class SoundApplication extends Application {

    public static final String PREFKEY_VERSION_CODE = "versionCode";
    public static final String PREFKEY_FIRSTRUN = "isFirstRun";
    static final String CLICK_IDENTIFIER = "lastClick";
    static final String LONG_CLICK_IDENTIFIER = "lastLongClick";
    private static final String YASB = "YASB";
    private static SoundPlayer soundPlayerInstance;
    private static Tracker tracker;
    private static SharedPreferences preferences;

    public static void log(String message) {
        Crashlytics.getInstance().core.log(Log.DEBUG, YASB, message);
    }

    public static SoundPlayer getSoundPlayerInstance() {
        return soundPlayerInstance;
    }

    public static void setSoundPlayerInstance(SoundPlayer instance) {
        SoundApplication.soundPlayerInstance = instance;
    }

    public static Tracker getTracker() {
        return tracker;
    }

    public static void setTracker(Tracker t) {
        SoundApplication.tracker = t;
    }

    public static SharedPreferences getPreferences() {
        return preferences;
    }

    public static void setPreferences(SharedPreferences p) {
        SoundApplication.preferences = p;
    }

    @Override
    public final void onCreate() {
        super.onCreate();
        setPreferences(PreferenceManager.getDefaultSharedPreferences(this));
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();
        // Initialize Fabric with the debug-disabled crashlytics.
        Fabric.with(this, crashlyticsKit);

        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        analytics.enableAutoActivityReports(this);
        analytics.setAppOptOut(getPreferences().getBoolean("opt_out", false) || BuildConfig.DEBUG);
        setTracker(analytics.newTracker("UA-26925696-3"));
        getTracker().enableExceptionReporting(true);
        getTracker().enableAdvertisingIdCollection(true);
        getTracker().enableAutoActivityTracking(true);
        AnalyticsTrackers.initialize(this);
    }
}
