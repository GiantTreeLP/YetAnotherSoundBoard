package org.gtlp.yasb;

import android.media.MediaPlayer;

public class SoundPlayer {
    public static MediaPlayer mediaPlayer;

    public static void init() {
        mediaPlayer = new MediaPlayer();
    }
}
