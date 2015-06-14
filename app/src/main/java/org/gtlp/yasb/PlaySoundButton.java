package org.gtlp.yasb;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;

import java.io.IOException;
import java.lang.reflect.Field;

import static android.view.View.OnClickListener;
import static android.widget.RemoteViews.RemoteView;
import static org.gtlp.yasb.SoundActivity.soundPlayerInstance;

@RemoteView
public class PlaySoundButton extends Button implements OnClickListener, View.OnLongClickListener {

    private String url;

    public PlaySoundButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setId(SoundActivity.uniqueId++);
        url = attrs.getAttributeValue("http://schemas.android.com/apk/res-auto", "url");
        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        R.raw r = new R.raw();
        Field f = null;
        SoundActivity.log("Hit " + getText());
        if (SoundActivity.tracker != null) {
            SoundActivity.tracker.setScreenName("YetAnotherSoundBoard ButtonFragment");
            SoundActivity.tracker.send(new HitBuilders.EventBuilder().setCategory("Sound").setAction("Play").setLabel(getText().toString()).build());
        }

        try {
            String field = getText().toString().replace(" ", "").replace("'", "").toLowerCase();
            f = R.raw.class.getDeclaredField(field);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        if (soundPlayerInstance != null) {
            SoundActivity.log("SoundPlayer instance is: " + soundPlayerInstance.toString());
            try {
                soundPlayerInstance.playSound(getContext(), Uri.parse("android.resource://org.gtlp.yasb/" + (int) f.get(r)));
            } catch (IOException | IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            soundPlayerInstance = new SoundPlayer();
            SoundActivity.log("SoundPlayer instance is: " + soundPlayerInstance.toString());
            try {
                soundPlayerInstance.playSound(getContext(), Uri.parse("android.resource://org.gtlp.yasb/R.raw." + (int) f.get(r)));
            } catch (IOException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        TextView tv = new TextView(getContext());
        String message = getResources().getString(R.string.msg_filename) + getText() + "\n\n";
        message += getResources().getString(R.string.msg_source) + url + "\n\n";
        SpannableString msg = new SpannableString(message);
        Linkify.addLinks(msg, Linkify.WEB_URLS);
        tv.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
        tv.setText(msg);
        tv.setAutoLinkMask(Linkify.WEB_URLS);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        builder.setTitle(R.string.dialog_info_title);
        builder.setView(tv, 24, 24, 24, 24);
        builder.setNeutralButton(getContext().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
        if (SoundActivity.tracker != null) {
            SoundActivity.tracker.setScreenName("InfoDialog-".concat(getText().toString()));
            SoundActivity.tracker.send(new HitBuilders.AppViewBuilder().build());
        }
        return true;
    }
}
