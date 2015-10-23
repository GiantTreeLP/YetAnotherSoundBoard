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
    public final Dialog onCreateDialog(Bundle bundle) {
        ScrollView scrollView = new ScrollView(getActivity());
        WebView webView = new WebView(getActivity());

        webView.loadData(getString(R.string.text_changelog), "text/html", "UTF-8");

        scrollView.addView(webView);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(scrollView).setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    SoundApplication.getPreferences().edit().putInt(SoundApplication.PREFKEY_VERSION_CODE, ChangelogDialogFragment.this.getActivity().getPackageManager().getPackageInfo(ChangelogDialogFragment.this.getActivity().getPackageName(), 0).versionCode).apply();
                } catch (PackageManager.NameNotFoundException ignored) {
                }
                dialog.dismiss();
            }
        });
        return builder.create();
    }
}
