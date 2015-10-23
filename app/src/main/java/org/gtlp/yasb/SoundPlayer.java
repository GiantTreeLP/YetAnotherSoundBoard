package org.gtlp.yasb;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.animation.LinearInterpolator;

import java.io.FileDescriptor;
import java.io.IOException;

class SoundPlayer extends MediaPlayer {

    public static final int MILLIS_PER_SECOND = 1000;
    public static final int SECONDS_PER_MINUTE = 60;
    private boolean isPrepared = false;
    private Seeker seeker;
    private String selectedSound;
    private ObjectAnimator objectAnimator;
    private MediaPlayerState mState;
    private SoundActivity parentActivity;

    public SoundPlayer(SoundActivity parent) {
        super();
        parentActivity = parent;
        setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                SoundApplication.log("SoundPlayer prepared");
                SoundPlayer.this.setState(MediaPlayerState.PREPARED);
                isPrepared = true;
            }
        });
        setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                SoundPlayer.this.setState(MediaPlayerState.PLAYBACK_COMPLETED);
                mp.seekTo(0);
                if (seeker != null) {
                    seeker.setPause(true);
                }
                parentActivity.getSeekBar().setProgress(0);
                parentActivity.getTimeText().setText(SoundPlayer.this.getFormattedProgressText());
                SoundPlayer.this.setPlayPauseButtonStates(true, false);
            }
        });
        setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                SoundPlayer.this.setState(MediaPlayerState.ERROR);
                return false;
            }
        });
        setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    private static String formatMillis(int millis) {
        int minutes = millis / MILLIS_PER_SECOND / SECONDS_PER_MINUTE;
        int seconds = millis / MILLIS_PER_SECOND;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public boolean isPrepared() {
        return isPrepared;
    }

    public Seeker getSeeker() {
        return seeker;
    }

    @Override
    public void reset() {
        super.reset();
        setState(MediaPlayerState.IDLE);
    }

    @Override
    public void setDataSource(Context context, Uri uri) throws IOException {
        super.setDataSource(context, uri);
        setState(MediaPlayerState.INITIALIZED);
    }

    @Override
    public void setDataSource(MediaDataSource dataSource) {
        super.setDataSource(dataSource);
        setState(MediaPlayerState.INITIALIZED);
    }

    @Override
    public void setDataSource(FileDescriptor fd, long offset, long length) throws IOException {
        super.setDataSource(fd, offset, length);
        setState(MediaPlayerState.INITIALIZED);
    }

    @Override
    public void setDataSource(String path) throws IOException {
        super.setDataSource(path);
        setState(MediaPlayerState.INITIALIZED);
    }

    @Override
    public void prepare() throws IOException {
        super.prepare();
        setState(MediaPlayerState.PREPARED);
    }

    @Override
    public void prepareAsync() {
        super.prepareAsync();
        setState(MediaPlayerState.PREPARING);
    }

    @Override
    public void stop() {
        super.stop();
        setState(MediaPlayerState.STOPPED);
    }

    @Override
    public void release() {
        super.release();
        setState(MediaPlayerState.END);
    }

    private void setPlayPauseButtonStates(boolean playButtonState, boolean pauseButtonState) {
        parentActivity.getPlayButton().setEnabled(playButtonState);
        parentActivity.getPauseButton().setEnabled(pauseButtonState);
    }

    public String getFormattedProgressText() {
        if (!getState().equals(MediaPlayerState.ERROR)) {
            return formatMillis(getCurrentPosition()) + "/" + formatMillis(getDuration());
        } else {
            return "";
        }
    }

    public void playSound(@NonNull Context context, @NonNull String assetName) throws IOException {
        if (assetName.equals(selectedSound) && isPrepared) {
            seekTo(0);
        } else {
            reset();
            AssetFileDescriptor assetFileDescriptor = context.getAssets().openFd(assetName);
            setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            assetFileDescriptor.close();
            selectedSound = assetName.replace(".mp3", "");
            parentActivity.getCurrent().setText(selectedSound);
            prepare();
        }
        start();
        setPlayPauseButtonStates(false, true);
    }

    @Override
    public void start() {
        super.start();
        setState(MediaPlayerState.STARTED);
        setPlayPauseButtonStates(false, true);
        parentActivity.getSeekBar().setEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            parentActivity.getSeekBar().setMax(getDuration());
            objectAnimator = ObjectAnimator.ofInt(parentActivity.getSeekBar(), "progress", getCurrentPosition(), getDuration());
            objectAnimator.setDuration(getDuration() - getCurrentPosition());
            objectAnimator.setInterpolator(new LinearInterpolator());
            objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (parentActivity.getTimeText() != null) {
                        parentActivity.getTimeText().setText(SoundPlayer.this.getFormattedProgressText());
                    }
                }
            });
            objectAnimator.start();
        } else {
            if (getSeeker() != null && getSeeker().getStatus() == AsyncTask.Status.RUNNING) {
                seeker.setPause(false);
            } else {
                seeker = new Seeker(parentActivity);
                seeker.execute();
            }
        }
    }

    @Override
    public void pause() {
        super.pause();
        setState(MediaPlayerState.PAUSED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && objectAnimator != null) {
            objectAnimator.setTarget(null);
        } else if (seeker != null) {
            seeker.setPause(true);
        }
        setPlayPauseButtonStates(true, false);
    }

    @Override
    public void seekTo(int time) {
        super.seekTo(time);
        setState(MediaPlayerState.PAUSED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && objectAnimator != null && parentActivity.getSeekBar() != null) {
            objectAnimator.setTarget(null);
        }
    }

    public MediaPlayerState getState() {
        return mState;
    }

    public void setState(MediaPlayerState newState) {
        mState = newState;
    }

    enum MediaPlayerState {
        IDLE,
        INITIALIZED,
        PREPARING,
        PREPARED,
        STARTED,
        STOPPED,
        PAUSED,
        PLAYBACK_COMPLETED,
        END,
        ERROR
    }
}
