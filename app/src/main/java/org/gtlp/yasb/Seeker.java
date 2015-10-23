package org.gtlp.yasb;

import android.os.AsyncTask;
import android.os.SystemClock;

class Seeker extends AsyncTask<Void, Void, Void> {

    public static final int IDLE_SLEEP_TIME = 250;
    public static final int SLEEP_TIME = 19;
    private boolean pause = false;
    private SoundActivity parentActivity;

    public Seeker(SoundActivity parent) {
        parentActivity = parent;
    }

    public final void setPause(boolean bPause) {
        this.pause = bPause;
    }

    @Override
    protected final Void doInBackground(Void... params) {
        while (!isCancelled()) {
            if (!pause && SoundApplication.getSoundPlayerInstance() != null && SoundApplication.getSoundPlayerInstance().isPrepared() && SoundApplication.getSoundPlayerInstance().isPlaying()) {
                publishProgress();
            }
            SystemClock.sleep(pause ? IDLE_SLEEP_TIME : SLEEP_TIME);
        }
        return null;
    }

    @Override
    protected final synchronized void onProgressUpdate(Void... values) {
        if (SoundApplication.getSoundPlayerInstance() != null) {
            if (parentActivity.getSeekBar() != null) {
                parentActivity.getSeekBar().setMax(SoundApplication.getSoundPlayerInstance().getDuration());
                parentActivity.getSeekBar().setProgress(SoundApplication.getSoundPlayerInstance().getCurrentPosition());
            }
            if (parentActivity.getTimeText() != null) {
                parentActivity.getTimeText().setText(SoundApplication.getSoundPlayerInstance().getFormattedProgressText());
            }
        }
    }

}
