package org.gtlp.yasb;

import android.os.AsyncTask;
import android.widget.SeekBar;
import android.widget.TextView;

import static org.gtlp.yasb.SoundActivity.soundPlayerInstance;

public class Seeker extends AsyncTask<Void, Void, Void> {

	@Override
	protected Void doInBackground(Void... params) {
		while (!isCancelled()) try {
			Thread.sleep(66);
			if ((soundPlayerInstance.isPrepared && soundPlayerInstance.isInitialized) && soundPlayerInstance.isPlaying())
				publishProgress();
		} catch (Exception ignored) {
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		super.onProgressUpdate(values);
		((SeekBar) soundPlayerInstance.viewContainer.findViewById(R.id.seekBar)).setMax(soundPlayerInstance.getDuration());
		((SeekBar) soundPlayerInstance.viewContainer.findViewById(R.id.seekBar)).setProgress(soundPlayerInstance.getCurrentPosition());
		((TextView) soundPlayerInstance.viewContainer.findViewById(R.id.timetext)).setText(formatMillis(soundPlayerInstance.getCurrentPosition()) + "/" + formatMillis(soundPlayerInstance.getDuration()));
	}

	private String formatMillis(int millis) {
		int minutes = millis / 1000 / 60;
		int seconds = millis / 1000;
		return minutes + ":" + (seconds < 10 ? "0" + seconds : seconds);
	}
}
