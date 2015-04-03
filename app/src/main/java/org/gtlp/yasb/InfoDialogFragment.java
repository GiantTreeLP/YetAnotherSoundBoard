package org.gtlp.yasb;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

public class InfoDialogFragment extends DialogFragment {

	private String message = "";

	public InfoDialogFragment() {

	}

	public void setInfo(FileInfo info, Activity activity) {
		message = activity.getString(R.string.msg_filename) + info.fileName + "\n\n";
		message += activity.getString(R.string.msg_name) + info.name + "\n\n";
		message += activity.getString(R.string.msg_source) + info.source + "\n\n";
		message += activity.getString(R.string.msg_hash) + info.remoteHash + "\n\n";
	}

	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		TextView tv = new TextView(getActivity());
		SpannableString msg = new SpannableString(message);
		Linkify.addLinks(msg, Linkify.WEB_URLS);
		tv.setTextAppearance(getActivity(), android.R.style.TextAppearance_Medium);
		tv.setText(msg);
		tv.setAutoLinkMask(Linkify.WEB_URLS);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		builder.setTitle(R.string.dialog_info_title);
		builder.setView(tv);
		builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				InfoDialogFragment.this.getDialog().dismiss();
			}
		});
		return builder.create();
	}
}
