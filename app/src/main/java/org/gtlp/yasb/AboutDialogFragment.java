package org.gtlp.yasb;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class AboutDialogFragment extends DialogFragment {

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_about_title);
        builder.setMessage(R.string.text_about);
        builder.setNeutralButton(android.R.string.ok, (dialog, which) -> AboutDialogFragment.this.getDialog().dismiss());
        return builder.create();
    }
}
