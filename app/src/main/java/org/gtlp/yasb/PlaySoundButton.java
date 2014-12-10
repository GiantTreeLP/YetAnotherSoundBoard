package org.gtlp.yasb;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;

import static android.view.View.OnClickListener;
import static android.widget.RemoteViews.RemoteView;

@RemoteView
public class PlaySoundButton extends Button implements OnClickListener, View.OnLongClickListener {

    public String[] info;
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlaySoundButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    public void setSound(File u) {
        if (BuildConfig.DEBUG) Log.d("YASB", "Added " + u.toString());
        resId = Uri.parse(u.toURI().toString());
    }


    @Override
    public void onClick(View v) {
        Tracker t = ((SoundActivity) SoundPlayer.getInstance().viewContainer).getTracker(SoundActivity.TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder().setCategory("Sound").setAction("Play").setLabel(info[2]).build());
        if (SoundPlayer.selectedSound == this.resId) {
            SoundPlayer.player.seekTo(0);
            SoundPlayer.getInstance().start();
        } else if (SoundPlayer.player != null) {
            SoundPlayer.player.release();
            SoundPlayer.getInstance().prepared = false;
            SoundPlayer.setPlayer(MediaPlayer.create(v.getContext(), resId), getText());
            SoundPlayer.selectedSound = resId;
            SoundPlayer.getInstance().initialized = true;
            SoundPlayer.getInstance().start();
        } else {
            SoundPlayer.setPlayer(MediaPlayer.create(v.getContext(), resId), getText());
            SoundPlayer.selectedSound = resId;
            SoundPlayer.getInstance().initialized = true;
            SoundPlayer.getInstance().start();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        InfoDialogFragment idf = new InfoDialogFragment();
        idf.setInfo(info, SoundPlayer.getInstance().viewContainer);
        idf.show(SoundPlayer.getInstance().viewContainer.getFragmentManager(), "InfoDialogFragment");
        Tracker t = ((SoundActivity) SoundPlayer.getInstance().viewContainer).getTracker(SoundActivity.TrackerName.APP_TRACKER);
        t.setScreenName("InfoDialog-".concat(info[2]));
        t.send(new HitBuilders.AppViewBuilder().build());
        return true;
    }
}
