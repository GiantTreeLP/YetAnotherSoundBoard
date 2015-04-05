package org.gtlp.yasb;

import android.os.AsyncTask;
import android.widget.SeekBar;
import android.widget.TextView;

import static org.gtlp.yasb.SoundActivity.soundPlayerInstance;

public class Seeker extends AsyncTask<Void, Void, Void> {

	public boolean pause = false;

	@Override
	protected Void doInBackground(Void... params) {
		while (!isCancelled()) {
			try {
				Thread.sleep(66);
			} catch (Exception e) {
				SoundActivity.Log("Exception in Seeker: " + e.getCause().getMessage());
				e.printStackTrace();
			}
			if (!pause) {
				if ((soundPlayerInstance.isPrepared && soundPlayerInstance.isInitialized) && soundPlayerInstance.isPlaying())
					publishProgress();
			}
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		super.onProgressUpdate(values);
		((SeekBar) soundPlayerInstance.viewContainer.findViewById(R.id.seekBar)).setMax(soundPlayerInstance.getDuration());
		((SeekBar) soundPlayerInstance.viewContainer.findViewById(R.id.seekBar)).setProgress(soundPlayerInstance.getCurrentPosition());
		((TextView) soundPlayerInstance.viewContainer.findViewById(R.id.timetext)).setText(SoundActivity.soundPlayerInstance.getFormattedProgressText());
	}

}
