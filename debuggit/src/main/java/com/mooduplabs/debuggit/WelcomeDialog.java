package com.mooduplabs.debuggit;

import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

public class WelcomeDialog extends DialogFragment implements View.OnClickListener {
    //region Consts

    public static final String TAG = WelcomeDialog.class.getSimpleName();

    //endregion

    //region Override Methods

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CustomDialog dialog = new CustomDialog(getActivity(), R.style.BrCustomDialog);
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_br_welcome, null);
        dialog.setContentView(v);

        setCancelable(false);

        MontserratTextView letsGoButton = (MontserratTextView) v.findViewById(R.id.welcome_lets_go_button);
        letsGoButton.setOnClickListener(this);

        return dialog;
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    @Override
    public void dismiss() {
        Utils.putBoolean(getActivity(), Constants.Keys.HAS_WELCOME_SCREEN, true);
        super.dismiss();
    }

    //endregion

}
