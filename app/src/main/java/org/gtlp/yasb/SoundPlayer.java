package org.gtlp.yasb;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.widget.SeekBar;
import android.widget.TextView;

public class SoundPlayer extends MediaPlayer {
	public Uri selectedSound;
	public boolean isPrepared = false;
	public boolean isInitialized = false;
	public FragmentActivity viewContainer;
	private Seeker seeker;

	public SoundPlayer(FragmentActivity view) {
		super();
		setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				isPrepared = true;
			}
		});
		setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.seekTo(0);
				if (seeker != null) {
					seeker.pause = true;
				}
				((SeekBar) viewContainer.findViewById(R.id.seekBar)).setProgress(0);
				((TextView) viewContainer.findViewById(R.id.timetext)).setText(getFormattedProgressText());
				viewContainer.findViewById(R.id.playButton).setEnabled(true);
				viewContainer.findViewById(R.id.pauseButton).setEnabled(false);
			}
		});
		setAudioStreamType(AudioManager.STREAM_MUSIC);
		viewContainer = view;
	}

	public String getFormattedProgressText() {
		return formatMillis(getCurrentPosition()) + "/" + formatMillis(getDuration());
	}

	public static String formatMillis(int millis) {
		int minutes = millis / 1000 / 60;
		int seconds = millis / 1000;
		return minutes + ":" + (seconds < 10 ? "0" + seconds : seconds);
	}

	public void start() {
		super.start();
		viewContainer.findViewById(R.id.playButton).setEnabled(false);
		viewContainer.findViewById(R.id.pauseButton).setEnabled(true);
		if (seeker != null) {
			if (seeker.getStatus() != AsyncTask.Status.RUNNING) {
				(seeker = new Seeker()).execute();
			} else {
				seeker.pause = false;
			}
		} else {
			(seeker = new Seeker()).execute();
		}
	}

	public void pause() {
		super.pause();
		viewContainer.findViewById(R.id.playButton).setEnabled(true);
		viewContainer.findViewById(R.id.pauseButton).setEnabled(false);
		if (seeker != null) {
			seeker.pause = true;
		}
	}
}
