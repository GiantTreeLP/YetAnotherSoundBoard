package org.gtlp.yasb;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.widget.SeekBar;
import android.widget.TextView;

public class SoundPlayer extends MediaPlayer {

    public static MediaPlayer player;
    private static SoundPlayer instance;

    Activity viewContainer;
    String currentlyPlaying;
    boolean prepared = false;
    boolean initialized = false;

    private SoundPlayer() {
        super();
        setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public SoundPlayer(Activity view) {
        this();
        viewContainer = view;
    }

    public static SoundPlayer getInstance() {
        return instance != null ? instance : (instance = new SoundPlayer());
    }

    public static void setInstance(SoundPlayer instance) {
        SoundPlayer.instance = instance;
    }

    public static void setPlayer(MediaPlayer player, CharSequence text) {
        SoundPlayer.player = player;
        ((TextView) getInstance().viewContainer.findViewById(R.id.current)).setText(text);

        SoundPlayer.player.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                getInstance().prepared = true;
            }
        });
        SoundPlayer.player.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.seekTo(0);
                getInstance().viewContainer.findViewById(R.id.playButton).setEnabled(true);
                getInstance().viewContainer.findViewById(R.id.pauseButton).setEnabled(false);
            }
        });
    }

    public void start() {
        if (player != null) {
            player.start();
            viewContainer.findViewById(R.id.playButton).setEnabled(false);
            viewContainer.findViewById(R.id.pauseButton).setEnabled(true);
            new Seeker().execute();
        }
    }

    public void pause() {
        if (player != null) {
            player.pause();
            viewContainer.findViewById(R.id.playButton).setEnabled(true);
            viewContainer.findViewById(R.id.pauseButton).setEnabled(false);
        }
    }

    private class Seeker extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            while (player != null) {
                if ((prepared && initialized) && player.isPlaying()) publishProgress();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            ((SeekBar) getInstance().viewContainer.findViewById(R.id.seekBar)).setMax(player.getDuration());
            ((SeekBar) getInstance().viewContainer.findViewById(R.id.seekBar)).setProgress(player.getCurrentPosition());
            ((TextView) getInstance().viewContainer.findViewById(R.id.timetext)).setText(formatMillis(player.getCurrentPosition()) + "/" + formatMillis(player.getDuration()));
        }

        private String formatMillis(int millis) {
            int minutes = millis / 1000 / 60;
            int seconds = millis / 1000;
            return minutes + ":" + (seconds <= 9 ? "0" + seconds : seconds);
        }
    }
}
