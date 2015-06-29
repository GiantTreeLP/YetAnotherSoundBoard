package org.gtlp.yasb;

import android.os.AsyncTask;
import android.os.SystemClock;

import static org.gtlp.yasb.SoundApplication.soundPlayerInstance;

public class Seeker extends AsyncTask<Void, Void, Void> {

	public boolean pause = false;

	@Override
	protected Void doInBackground(Void... params) {
		while (!isCancelled()) {
			long sleepTime = System.nanoTime();
			if (!pause && soundPlayerInstance.get().isPrepared && soundPlayerInstance.get().isPlaying()) {
					publishProgress();
			}
			long toSleep = 19 - (System.nanoTime() - sleepTime) / 1000000;
			SystemClock.sleep(toSleep > 0 ? toSleep : 19);
		}
		return null;
	}

	@Override
	protected synchronized void onProgressUpdate(Void... values) {
		if (soundPlayerInstance.get() != null) {
			if (SoundActivity.seekBar != null) {
				SoundActivity.seekBar.setMax(soundPlayerInstance.get().getDuration());
				SoundActivity.seekBar.setProgress(soundPlayerInstance.get().getCurrentPosition());
			}
			if (SoundActivity.timeText != null) {
				SoundActivity.timeText.setText(SoundApplication.soundPlayerInstance.get().getFormattedProgressText());
			}
		}
	}

}
