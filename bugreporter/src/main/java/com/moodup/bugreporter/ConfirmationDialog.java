package com.moodup.bugreporter;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import butterknife.ButterKnife;

public class ConfirmationDialog extends DialogFragment {
    //region Consts
    protected static final String TAG = ConfirmationDialog.class.getSimpleName();
    protected static final int TYPE_FAILURE = 0;
    protected static final int TYPE_SUCCESS = 1;
    //endregion
    //region Fields
    //endregion

    //region Override Methods

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CustomDialog dialog = new CustomDialog(getActivity(), R.style.CustomDialog);
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_confirmation, null);
        ButterKnife.bind(this, v);
        dialog.setContentView(v);

        initViews(v);

        return dialog;
    }

    //endregion

    //region Events

    //endregion

    //region Methods
    protected static ConfirmationDialog newInstance(int type) {
        ConfirmationDialog dialog = new ConfirmationDialog();

        Bundle bundle = new Bundle();
        bundle.putInt("type", type);

        dialog.setArguments(bundle);

        return dialog;
    }

    protected static ConfirmationDialog newInstance(String errorMessage) {
        ConfirmationDialog dialog = new ConfirmationDialog();

        Bundle bundle = new Bundle();
        bundle.putString("error", errorMessage);
        bundle.putInt("type", TYPE_FAILURE);

        dialog.setArguments(bundle);
        return dialog;
    }

    private void initViews(View view) {
        final int type = getArguments().getInt("type", TYPE_SUCCESS);

        ImageView icon = ButterKnife.findById(view, R.id.confirmation_dialog_icon);
        MontserratTextView message = ButterKnife.findById(view, R.id.confirmation_dialog_message);
        MontserratTextView okButton = ButterKnife.findById(view, R.id.confirmation_dialog_ok_button);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == TYPE_SUCCESS) {
                    ((DialogFragment)getParentFragment()).dismiss();
                }
                dismiss();
            }
        });

        icon.setRotation(type == TYPE_SUCCESS ? 0 : 180.0f);
        String errorMessage = getArguments().getString("error", "");
        if(errorMessage.isEmpty()) {
            message.setText(getString(type == TYPE_SUCCESS ? R.string.confirmation_success : R.string.confirmation_failure));
        } else {
            message.setText(errorMessage);
        }
    }
    //endregion


}