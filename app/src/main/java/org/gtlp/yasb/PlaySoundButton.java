package org.gtlp.yasb;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;

import static android.view.View.OnClickListener;
import static android.widget.RemoteViews.RemoteView;

@RemoteView
public class PlaySoundButton extends Button implements OnClickListener {

    private int index = 1;
    private Uri resId;

    public PlaySoundButton(Context context) {
        this(context, null);
    }

    public PlaySoundButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PlaySoundButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
        init(context, attrs);
    }

    @TargetApi(21)
    public PlaySoundButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOnClickListener(this);
    }

    public void setSound(File u) {
        if (BuildConfig.DEBUG) Log.d("YASB", "Added " + u.toString());
        //setText(u.getName().replace(".mp3", ""));
        resId = Uri.parse(u.toURI().toString());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        //   setTranslationY(getIndex() * getHeight());
        // } else {
        //super.onLayout(true, 10, getIndex() * getHeight(), getLeft() + getWidth(), getIndex() * getHeight() + getHeight());
        // }
        //if (BuildConfig.DEBUG) Log.d("YASB", "Height: " + getHeight());
    }

    @Override
    public void onClick(View v) {
        SoundPlayer.mediaPlayer.reset();
        SoundPlayer.mediaPlayer = MediaPlayer.create(v.getContext(), resId);
        SoundPlayer.mediaPlayer.start();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int i) {
        index = i;
    }

}
