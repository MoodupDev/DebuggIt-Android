package com.mooduplabs.debuggit;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

public class WelcomeDialog extends DialogFragment {
    //region Consts

    public static final String TAG = WelcomeDialog.class.getSimpleName();

    //endregion

    //region Override Methods

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CustomDialog dialog = new CustomDialog(getActivity(), R.style.BrCustomDialog);
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_br_welcome, null);
        dialog.setContentView(v);

        return dialog;
    }

    //endregion

}
