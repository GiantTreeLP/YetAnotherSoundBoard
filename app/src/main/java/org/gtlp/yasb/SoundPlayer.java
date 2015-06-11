package org.gtlp.yasb;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.IOException;

public class SoundPlayer extends MediaPlayer {
	public Uri selectedSound;
	public boolean isPrepared = false;
	public Seeker seeker;

	public SoundPlayer() {
		super();
		setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				SoundActivity.log("SoundPlayer prepared");
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
				SoundActivity.seekBar.setProgress(0);
				SoundActivity.timeText.setText(getFormattedProgressText());
				setPlayPauseButtonStates(true, false);
			}
		});
		setAudioStreamType(AudioManager.STREAM_MUSIC);
	}

	public static String formatMillis(int millis) {
		int minutes = millis / 1000 / 60;
		int seconds = millis / 1000;
		return minutes + ":" + (seconds < 10 ? "0" + seconds : seconds);
	}

	protected void setPlayPauseButtonStates(boolean playButtonState, boolean pauseButtonState) {
		SoundActivity.playButton.setEnabled(playButtonState);
		SoundActivity.pauseButton.setEnabled(pauseButtonState);
	}

	public String getFormattedProgressText() {
		return formatMillis(getCurrentPosition()) + "/" + formatMillis(getDuration());
	}

	public void playSound(@NonNull Context context, @NonNull Uri uri) throws IOException {
		if (uri.equals(selectedSound) && isPrepared) {
			seekTo(0);
			start();
		} else {
			reset();
			setDataSource(context, uri);
			selectedSound = uri;
			prepare();
			start();
		}
		setPlayPauseButtonStates(false, true);
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
		setPlayPauseButtonStates(true, false);
		if (seeker != null) {
			seeker.pause = true;
		}
	}
}
