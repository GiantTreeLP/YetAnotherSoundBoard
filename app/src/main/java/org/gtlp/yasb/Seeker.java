package org.gtlp.yasb;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.widget.SeekBar;
import android.widget.TextView;

import static org.gtlp.yasb.SoundActivity.soundPlayerInstance;

public class Seeker extends AsyncTask<Void, Void, Void> {

	public boolean pause = false;
	private SeekBar seekBar;
	private TextView textView;

	@Override
	protected Void doInBackground(Void... params) {
		seekBar = ((SeekBar) soundPlayerInstance.viewContainer.findViewById(R.id.seekBar));
		textView = ((TextView) soundPlayerInstance.viewContainer.findViewById(R.id.timetext));
		while (!isCancelled()) {
			long sleepTime = System.nanoTime();
			if (!pause) {
				if ((soundPlayerInstance.isPrepared && soundPlayerInstance.isInitialized) && soundPlayerInstance.isPlaying())
					publishProgress();
			}
			long toSleep = 16 - (System.nanoTime() - sleepTime) / 1000000;
			SystemClock.sleep(toSleep > 0 ? toSleep : 16);
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		synchronized (this) {
			seekBar.setMax(soundPlayerInstance.getDuration());
			seekBar.setProgress(soundPlayerInstance.getCurrentPosition());
			textView.setText(SoundActivity.soundPlayerInstance.getFormattedProgressText());
		}
	}

}
