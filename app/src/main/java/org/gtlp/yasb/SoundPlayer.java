package org.gtlp.yasb;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.widget.SeekBar;
import android.widget.TextView;

public class SoundPlayer extends MediaPlayer {
    public Uri selectedSound;
    FragmentActivity viewContainer;
    boolean isPrepared = false;
    boolean isInitialized = false;
    private Seeker seeker = new Seeker();

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
                ((SeekBar) viewContainer.findViewById(R.id.seekBar)).setProgress(0);
                viewContainer.findViewById(R.id.playButton).setEnabled(true);
                viewContainer.findViewById(R.id.pauseButton).setEnabled(false);
            }
        });
        setAudioStreamType(AudioManager.STREAM_MUSIC);
        viewContainer = view;
    }

    public void start() {
        super.start();
        viewContainer.findViewById(R.id.playButton).setEnabled(false);
        viewContainer.findViewById(R.id.pauseButton).setEnabled(true);
        if (seeker.getStatus() != AsyncTask.Status.RUNNING) (seeker = new Seeker()).execute();
    }

    public void pause() {
        super.pause();
        viewContainer.findViewById(R.id.playButton).setEnabled(true);
        viewContainer.findViewById(R.id.pauseButton).setEnabled(false);
        seeker.cancel(true);
    }
}
