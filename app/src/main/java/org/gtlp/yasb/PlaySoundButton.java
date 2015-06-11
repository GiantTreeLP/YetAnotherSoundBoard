package org.gtlp.yasb;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.HitBuilders;

import java.io.File;
import java.io.IOException;

import static android.view.View.OnClickListener;
import static android.widget.RemoteViews.RemoteView;
import static org.gtlp.yasb.SoundActivity.soundPlayerInstance;

@RemoteView
public class PlaySoundButton extends Button implements OnClickListener, View.OnLongClickListener {

    private FileInfo info;
    private Uri resId;

    public PlaySoundButton(Context context) {
        this(context, null);
    }

    public PlaySoundButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    public PlaySoundButton(Context applicationContext, FileInfo fileInfo, View dummy, int i) {
        this(applicationContext, null);
        setId(SoundActivity.uniqueId++);
        this.info = fileInfo;
        setText(info.name);
        setSound(info.localFile);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setBackground(applicationContext.getDrawable(R.drawable.sound_button_shape));
        } else {
            setBackgroundResource(R.drawable.sound_button_shape);
        }
        SoundActivity.log("Below " + (i > 0 ? getId() - 1 : "Nothing"));
    }

    public void setSound(File u) {
        SoundActivity.log("Added " + u.toString());
        resId = Uri.parse(u.toURI().toString());
    }

    @Override
    public void onClick(View v) {
        SoundActivity.log("Hit " + resId.toString());
        SoundActivity.log("SoundPlayer instance is: " + soundPlayerInstance.toString());
        if (SoundActivity.tracker != null) {
            SoundActivity.tracker.setScreenName("YetAnotherSoundBoard ButtonFragment");
            SoundActivity.tracker.send(new HitBuilders.EventBuilder().setCategory("Sound").setAction("Play").setLabel(info.name).build());
        }
        if (soundPlayerInstance != null) {
            try {
                soundPlayerInstance.playSound(getContext(), resId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            soundPlayerInstance = new SoundPlayer();
            try {
                soundPlayerInstance.playSound(getContext(), resId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        InfoDialogFragment idf = new InfoDialogFragment();
        idf.setInfo(info, getContext());
        idf.show(idf.getFragmentManager(), "InfoDialogFragment");
        if (SoundActivity.tracker != null) {
            SoundActivity.tracker.setScreenName("InfoDialog-".concat(info.name));
            SoundActivity.tracker.send(new HitBuilders.AppViewBuilder().build());
        }
        return true;
    }
}
