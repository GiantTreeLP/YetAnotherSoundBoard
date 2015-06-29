package org.gtlp.yasb;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.webkit.WebView;
import android.widget.ScrollView;

public class ChangelogDialogFragment extends DialogFragment {
	@NonNull
	public Dialog onCreateDialog(Bundle bundle) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		ScrollView scrollView = new ScrollView(getActivity());
		WebView webView = new WebView(getActivity());

		webView.loadData(getString(R.string.text_changelog), "text/html", "UTF-8");

		scrollView.addView(webView);
		builder.setView(scrollView);
		builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					SoundApplication.preferences.edit().putInt(SoundActivity.PREFKEY_VERSION_CODE, getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode).apply();
				} catch (PackageManager.NameNotFoundException e) {
					e.printStackTrace();
				}
				dialog.dismiss();
			}
		});
		return builder.create();
	}
}
