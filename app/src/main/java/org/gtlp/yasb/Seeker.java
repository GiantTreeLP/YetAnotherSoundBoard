package org.gtlp.yasb;

import android.os.AsyncTask;
import android.os.SystemClock;

import static org.gtlp.yasb.SoundApplication.soundPlayerInstance;

public class Seeker extends AsyncTask<Void, Void, Void> {

    public boolean pause = false;

    @Override
    protected Void doInBackground(Void... params) {
        while (!isCancelled()) {
            if (!pause && soundPlayerInstance != null && soundPlayerInstance.isPrepared && soundPlayerInstance.isPlaying()) {
                publishProgress();
            }
            SystemClock.sleep(pause ? 250 : 19);
        }
        return null;
    }

    @Override
    protected synchronized void onProgressUpdate(Void... values) {
        if (soundPlayerInstance != null) {
            if (SoundActivity.seekBar != null) {
                SoundActivity.seekBar.setMax(soundPlayerInstance.getDuration());
                SoundActivity.seekBar.setProgress(soundPlayerInstance.getCurrentPosition());
            }
            if (SoundActivity.timeText != null) {
                SoundActivity.timeText.setText(SoundApplication.soundPlayerInstance.getFormattedProgressText());
            }
        }
    }

}
