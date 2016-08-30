package com.mooduplabs.debuggit;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;


public class LoadingDialog extends DialogFragment {
    //region Consts

    protected static final String TAG = LoadingDialog.class.getSimpleName();

    //endregion

    //region Fields

    private View.OnClickListener onCancelClickListener;

    //endregion

    //region Override Methods

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CustomDialog dialog = new CustomDialog(getActivity(), R.style.BrCustomDialog);
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_loading, null);
        dialog.setContentView(v);

        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initViews(v);

        return dialog;
    }

    //endregion

    //region Events

    //endregion

    //region Methods

    protected static LoadingDialog newInstance(String message) {
        return newInstance(message, null);
    }

    protected static LoadingDialog newInstance(String message, View.OnClickListener onCancelClickListener) {

        Bundle bundle = new Bundle();
        bundle.putString("message", message);

        LoadingDialog fragment = new LoadingDialog();
        fragment.setArguments(bundle);
        fragment.onCancelClickListener = onCancelClickListener;
        return fragment;
    }

    private void initViews(View view) {
        MontserratTextView message = (MontserratTextView) view.findViewById(R.id.confirmation_dialog_message);
        MontserratTextView cancelButton = (MontserratTextView) view.findViewById(R.id.loading_dialog_cancel_button);

        message.setText(getArguments().getString("message", ""));
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onCancelClickListener != null) {
                    onCancelClickListener.onClick(v);
                }
                dismiss();
            }
        });
    }
    //endregion


}