package org.gtlp.yasb;

import android.os.AsyncTask;
import android.widget.SeekBar;
import android.widget.TextView;

public class Seeker extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... params) {
        while (!isCancelled()) try {
            Thread.sleep(66);
            if ((SoundActivity.soundPlayerInstance.isPrepared && SoundActivity.soundPlayerInstance.isInitialized) && SoundActivity.soundPlayerInstance.isPlaying())
                publishProgress();
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        ((SeekBar) SoundActivity.soundPlayerInstance.viewContainer.findViewById(R.id.seekBar)).setMax(SoundActivity.soundPlayerInstance.getDuration());
        ((SeekBar) SoundActivity.soundPlayerInstance.viewContainer.findViewById(R.id.seekBar)).setProgress(SoundActivity.soundPlayerInstance.getCurrentPosition());
        ((TextView) SoundActivity.soundPlayerInstance.viewContainer.findViewById(R.id.timetext)).setText(formatMillis(SoundActivity.soundPlayerInstance.getCurrentPosition()) + "/" + formatMillis(SoundActivity.soundPlayerInstance.getDuration()));
    }

    private String formatMillis(int millis) {
        int minutes = millis / 1000 / 60;
        int seconds = millis / 1000;
        return minutes + ":" + (seconds < 10 ? "0" + seconds : seconds);
    }
}
