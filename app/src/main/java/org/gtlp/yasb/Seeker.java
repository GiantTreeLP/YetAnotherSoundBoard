package org.gtlp.yasb;

import android.os.AsyncTask;
import android.os.SystemClock;

class Seeker extends AsyncTask<Void, Void, Void> {

    public static final int IDLE_SLEEP_TIME = 250;
    public static final int SLEEP_TIME = 19;
    private boolean pause = false;

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
            if (SoundActivity.getSeekBar() != null) {
                SoundActivity.getSeekBar().setMax(SoundApplication.getSoundPlayerInstance().getDuration());
                SoundActivity.getSeekBar().setProgress(SoundApplication.getSoundPlayerInstance().getCurrentPosition());
            }
            if (SoundActivity.getTimeText() != null) {
                SoundActivity.getTimeText().setText(SoundApplication.getSoundPlayerInstance().getFormattedProgressText());
            }
        }
    }

}
