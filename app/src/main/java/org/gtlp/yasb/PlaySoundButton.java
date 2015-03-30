package org.gtlp.yasb;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.view.View.OnClickListener;
import static android.widget.RemoteViews.RemoteView;
import static org.gtlp.yasb.SoundActivity.*;
import static org.gtlp.yasb.SoundActivity.soundPlayerInstance;

@RemoteView
public class PlaySoundButton extends Button implements OnClickListener, View.OnLongClickListener {

    public String[] info;
    private Uri resId;

    public PlaySoundButton(Context context) {
        this(context, null);
    }

    public PlaySoundButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlaySoundButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlaySoundButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public PlaySoundButton(Context applicationContext, ArrayList<String[]> remoteHashes, ArrayList<File> localFiles, TextView dummy, int i) {
        this(applicationContext, null);
        setId(UniqueID.counter++);
        info = remoteHashes.get(i);
        setText(remoteHashes.get(i)[2]);
        setSound(localFiles.get(i));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (i % 2 == 0) {
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_RIGHT, dummy.getId());
        } else {
            params.addRule(RelativeLayout.RIGHT_OF, dummy.getId());
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
        params.addRule(i > 0 ? RelativeLayout.BELOW : RelativeLayout.ALIGN_PARENT_TOP, i > 1 ? getId() - 2 : RelativeLayout.TRUE);
        setVisibility(VISIBLE);

        setLayoutParams(params);
        if (BuildConfig.DEBUG)
            Log.d(YASB, "Below " + (i > 0 ? getId() - 1 : "Nothing"));
    }

    private void init() {
        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    public void setSound(File u) {
        if (BuildConfig.DEBUG) Log.d("YASB", "Added " + u.toString());
        resId = Uri.parse(u.toURI().toString());
    }


    @Override
    public void onClick(View v) {
        Tracker t = ((SoundActivity) soundPlayerInstance.viewContainer).getTracker(TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder().setCategory("Sound").setAction("Play").setLabel(info[2]).build());
        if (soundPlayerInstance.selectedSound == resId) {
            soundPlayerInstance.seekTo(0);
            soundPlayerInstance.start();
            return;
        } else if (soundPlayerInstance != null) {
            soundPlayerInstance.stop();
        }
        ((TextView) soundPlayerInstance.viewContainer.findViewById(R.id.current)).setText(getText());
        try {
            soundPlayerInstance.reset();
            soundPlayerInstance.setDataSource(v.getContext(), resId);
            soundPlayerInstance.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        soundPlayerInstance.selectedSound = resId;
        soundPlayerInstance.isInitialized = true;
        soundPlayerInstance.start();
    }

    @Override
    public boolean onLongClick(View v) {
        InfoDialogFragment idf = new InfoDialogFragment();
        idf.setInfo(info, soundPlayerInstance.viewContainer);
        idf.show(soundPlayerInstance.viewContainer.getSupportFragmentManager(), "InfoDialogFragment");
        Tracker t = ((SoundActivity) soundPlayerInstance.viewContainer).getTracker(TrackerName.APP_TRACKER);
        t.setScreenName("InfoDialog-".concat(info[2]));
        t.send(new HitBuilders.AppViewBuilder().build());
        return true;
    }
}
