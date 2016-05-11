package com.moodup.bugreporter;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import butterknife.ButterKnife;

public class LoadingDialog extends DialogFragment {
    //region Consts
    protected static final String TAG = LoadingDialog.class.getSimpleName();
    //endregion
    //region Fields
    //endregion

    //region Override Methods

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CustomDialog dialog = new CustomDialog(getActivity(), R.style.CustomDialog);
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_loading, null);
        ButterKnife.bind(this, v);
        dialog.setContentView(v);

        initViews(v);

        return dialog;
    }

    //endregion

    //region Events

    //endregion

    //region Methods
    private void initViews(View view) {
        MontserratTextView cancelButton = ButterKnife.findById(view, R.id.loading_dialog_cancel_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
    //endregion


}