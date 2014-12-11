package org.gtlp.yasb;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.widget.SeekBar;
import android.widget.TextView;

public class SoundPlayer extends MediaPlayer {

    public static MediaPlayer player;
    public static Uri selectedSound;
    private static SoundPlayer instance;
    FragmentActivity viewContainer;
    boolean prepared = false;
    boolean initialized = false;
    private Seeker seeker = new Seeker();

    private SoundPlayer() {
        super();
        setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public SoundPlayer(FragmentActivity view) {
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
                ((SeekBar) getInstance().viewContainer.findViewById(R.id.seekBar)).setProgress(0);
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
            if (seeker.getStatus() != AsyncTask.Status.RUNNING) (seeker = new Seeker()).execute();
        }
    }

    public void pause() {
        if (player != null) {
            player.pause();
            viewContainer.findViewById(R.id.playButton).setEnabled(true);
            viewContainer.findViewById(R.id.pauseButton).setEnabled(false);
            seeker.cancel(true);
        }
    }

    private class Seeker extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            while ((player != null) && !isCancelled()) try {
                Thread.sleep(66);
                if ((prepared && initialized) && player.isPlaying()) publishProgress();
            } catch (Exception ignored) {
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
