package com.mooduplabs.debuggit;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class ConfirmationDialog extends DialogFragment {
    //region Consts

    protected static final String TAG = ConfirmationDialog.class.getSimpleName();
    protected static final int TYPE_FAILURE = 0;
    protected static final int TYPE_SUCCESS = 1;
    private static final String MESSAGE = "message";
    private static final String TYPE = "type";

    //endregion

    //region Fields

    private View.OnClickListener onOkClickListener;

    //endregion

    //region Override Methods

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CustomDialog dialog = new CustomDialog(getActivity(), R.style.BrCustomDialog);
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_br_confirmation, null);
        dialog.setContentView(v);

        initViews(v);

        return dialog;
    }

    //endregion

    //region Methods
    protected static ConfirmationDialog newInstance(int type) {
        ConfirmationDialog dialog = new ConfirmationDialog();

        Bundle bundle = new Bundle();
        bundle.putInt(TYPE, type);

        dialog.setArguments(bundle);

        return dialog;
    }

    protected static ConfirmationDialog newInstance(String message, boolean error) {
        ConfirmationDialog dialog = new ConfirmationDialog();

        Bundle bundle = new Bundle();
        bundle.putString(MESSAGE, message);
        bundle.putInt(TYPE, error ? TYPE_FAILURE : TYPE_SUCCESS);

        dialog.setArguments(bundle);
        return dialog;
    }

    protected static ConfirmationDialog newInstance(String message, boolean error, View.OnClickListener onOkClickListener) {
        ConfirmationDialog dialog = ConfirmationDialog.newInstance(message, error);
        dialog.setOnOkClickListener(onOkClickListener);
        return dialog;
    }

    protected void setOnOkClickListener(View.OnClickListener onOkClickListener) {
        this.onOkClickListener = onOkClickListener;
    }

    private void initViews(View view) {
        final int type = getArguments().getInt(TYPE, TYPE_SUCCESS);

        ImageView icon = (ImageView) view.findViewById(R.id.confirmation_dialog_icon);
        MontserratTextView message = (MontserratTextView) view.findViewById(R.id.confirmation_dialog_message);
        MontserratTextView okButton = (MontserratTextView) view.findViewById(R.id.confirmation_dialog_ok_button);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == TYPE_SUCCESS) {
                    ((DialogFragment)getParentFragment()).dismiss();
                }
                if(onOkClickListener != null) {
                    onOkClickListener.onClick(null);
                }
                dismiss();
            }
        });

        icon.setRotation(type == TYPE_SUCCESS ? 0 : 180.0f);
        String text = getArguments().getString(MESSAGE, "");
        if(text.isEmpty()) {
            message.setText(getString(type == TYPE_SUCCESS ? R.string.br_confirmation_success : R.string.br_confirmation_failure));
        } else {
            message.setText(text);
        }
    }
    //endregion


}