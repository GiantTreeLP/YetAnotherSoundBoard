package org.gtlp.yasb;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;

import java.io.IOException;

import static android.view.View.OnClickListener;
import static android.widget.RemoteViews.RemoteView;
import static org.gtlp.yasb.SoundApplication.soundPlayerInstance;

@RemoteView
public class PlaySoundButton extends Button implements OnClickListener, View.OnLongClickListener {

    private String url;
    private String file;

    public PlaySoundButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.PlaySoundButton,
                0, 0);

        try {
            url = a.getString(R.styleable.PlaySoundButton_url);
            file = a.getString(R.styleable.PlaySoundButton_file);
        } finally {
            a.recycle();
        }

        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        SoundApplication.log("Hit " + getText());
        if (SoundApplication.tracker != null) {
            SoundApplication.tracker.setScreenName("YetAnotherSoundBoard ButtonFragment");
            SoundApplication.tracker.send(new HitBuilders.EventBuilder().setCategory("Sound").setAction("Play").setLabel(getText().toString()).build());
        }
        Crashlytics.getInstance().core.setString(SoundApplication.CLICK_IDENTIFIER, file);
        if (soundPlayerInstance == null) {
            soundPlayerInstance = new SoundPlayer();
        }
        SoundApplication.log("SoundPlayer instance is: " + soundPlayerInstance.toString());
        try {
            soundPlayerInstance.playSound(getContext(), file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        TextView tv = new TextView(getContext());
        String message = getResources().getString(R.string.msg_name) + getText() + "\n\n";
        message += getResources().getString(R.string.msg_source) + url + "\n\n";
        SpannableString msg = new SpannableString(message);
        Linkify.addLinks(msg, Linkify.WEB_URLS);
        tv.setTextAppearance(android.R.style.TextAppearance_Medium);
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
        if (SoundApplication.tracker != null) {
            SoundApplication.tracker.setScreenName("InfoDialog-".concat(getText().toString()));
            SoundApplication.tracker.send(new HitBuilders.AppViewBuilder().build());
        }
        Crashlytics.getInstance().core.setString(SoundApplication.LONG_CLICK_IDENTIFIER, "Info-".concat(file));
        return true;
    }
}
