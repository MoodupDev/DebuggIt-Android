package com.moodup.bugreporter;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class LoadingDialog extends DialogFragment {
    //region Consts
    protected static final String TAG = LoadingDialog.class.getSimpleName();
    //endregion
    //region Fields
    private Unbinder unbinder;
    //endregion

    //region Override Methods

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CustomDialog dialog = new CustomDialog(getActivity(), R.style.CustomDialog);
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_loading, null);
        unbinder = ButterKnife.bind(this, v);
        dialog.setContentView(v);

        initViews(v);

        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    //endregion

    //region Events

    //endregion

    //region Methods
    protected static LoadingDialog newInstance(String message) {
        LoadingDialog dialog = new LoadingDialog();

        Bundle bundle = new Bundle();
        bundle.putString("message", message);

        dialog.setArguments(bundle);

        return dialog;
    }

    private void initViews(View view) {
        MontserratTextView message = ButterKnife.findById(view, R.id.confirmation_dialog_message);
        MontserratTextView cancelButton = ButterKnife.findById(view, R.id.loading_dialog_cancel_button);

        message.setText(getArguments().getString("message", ""));
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
    //endregion


}