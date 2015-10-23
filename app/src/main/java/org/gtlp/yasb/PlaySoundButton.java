package org.gtlp.yasb;

import android.content.Context;
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

@RemoteView
public class PlaySoundButton extends Button implements OnClickListener, View.OnLongClickListener {

    public static final int VIEW_SPACING = 24;
    private String url;
    private String file;
    private SoundActivity parent;

    public PlaySoundButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (context instanceof SoundActivity) {
            parent = (SoundActivity) context;
        } else {
            parent = null;
        }

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
    public final void onClick(View v) {
        SoundApplication.log("Hit " + getText());
        if (SoundApplication.getTracker() != null) {
            SoundApplication.getTracker().setScreenName("YetAnotherSoundBoard ButtonFragment");
            SoundApplication.getTracker().send(new HitBuilders.EventBuilder().setCategory("Sound").setAction("Play").setLabel(getText().toString()).build());
        }
        Crashlytics.getInstance().core.setString(SoundApplication.CLICK_IDENTIFIER, file);
        if (SoundApplication.getSoundPlayerInstance() == null && getContext() instanceof SoundActivity) {
            SoundApplication.setSoundPlayerInstance(new SoundPlayer(parent));
            parent.getSeekBar().setEnabled(true);
        }
        SoundApplication.log("SoundPlayer instance is: " + SoundApplication.getSoundPlayerInstance().toString());
        try {
            SoundApplication.getSoundPlayerInstance().playSound(getContext(), file);
        } catch (IOException ignored) {
        }
    }

    @Override
    public final boolean onLongClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        TextView tv = new TextView(getContext());
        String message = getResources().getString(R.string.msg_name) + getText() + "\n\n";
        message += getResources().getString(R.string.msg_source) + url + "\n\n";
        SpannableString msg = new SpannableString(message);
        Linkify.addLinks(msg, Linkify.WEB_URLS);
        tv.setText(msg);
        tv.setAutoLinkMask(Linkify.WEB_URLS);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        builder.setTitle(R.string.dialog_info_title);
        builder.setView(tv, VIEW_SPACING, VIEW_SPACING, VIEW_SPACING, VIEW_SPACING);
        builder.setNeutralButton(getContext().getString(android.R.string.ok), (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
        if (SoundApplication.getTracker() != null) {
            SoundApplication.getTracker().setScreenName("InfoDialog-".concat(getText().toString()));
            SoundApplication.getTracker().send(new HitBuilders.AppViewBuilder().build());
        }
        Crashlytics.getInstance().core.setString(SoundApplication.LONG_CLICK_IDENTIFIER, "Info-".concat(file));
        return true;
    }
}
