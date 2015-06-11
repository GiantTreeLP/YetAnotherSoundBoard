package org.gtlp.yasb;

import android.os.AsyncTask;
import android.os.SystemClock;

import static org.gtlp.yasb.SoundActivity.soundPlayerInstance;

public class Seeker extends AsyncTask<Void, Void, Void> {

	public boolean pause = false;

	@Override
	protected Void doInBackground(Void... params) {
		while (!isCancelled()) {
			long sleepTime = System.nanoTime();
			if (!pause && soundPlayerInstance.isPrepared && soundPlayerInstance.isPlaying()) {
					publishProgress();
			}
			long toSleep = 19 - (System.nanoTime() - sleepTime) / 1000000;
			SystemClock.sleep(toSleep > 0 ? toSleep : 19);
		}
		return null;
	}

	@Override
	protected synchronized void onProgressUpdate(Void... values) {
		if (SoundActivity.seekBar != null) {
			SoundActivity.seekBar.setMax(soundPlayerInstance.getDuration());
			SoundActivity.seekBar.setProgress(soundPlayerInstance.getCurrentPosition());
		}
		if (SoundActivity.timeText != null) {
			SoundActivity.timeText.setText(SoundActivity.soundPlayerInstance.getFormattedProgressText());
		}
	}

}
