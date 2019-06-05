package com.mooduplabs.debuggit;

import android.app.Dialog;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.DialogFragment;

public class ConfirmationDialog extends DialogFragment {


    protected static final String TAG = ConfirmationDialog.class.getSimpleName();
    protected static final int TYPE_FAILURE = 0;
    protected static final int TYPE_SUCCESS = 1;
    private static final String MESSAGE = "message";
    private static final String CONTAINS_LINKS = "contains_links";
    private static final String TYPE = "type";


    private View.OnClickListener onOkClickListener;


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

    protected static ConfirmationDialog newInstance(String message, boolean error, boolean link) {
        ConfirmationDialog dialog = new ConfirmationDialog();

        Bundle bundle = new Bundle();
        bundle.putString(MESSAGE, message);
        bundle.putBoolean(CONTAINS_LINKS, link);
        bundle.putInt(TYPE, error ? TYPE_FAILURE : TYPE_SUCCESS);

        dialog.setArguments(bundle);
        return dialog;
    }

    protected static ConfirmationDialog newInstance(String message, boolean error, View.OnClickListener onOkClickListener) {
        ConfirmationDialog dialog = ConfirmationDialog.newInstance(message, error);
        dialog.setOnOkClickListener(onOkClickListener);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CustomDialog dialog = new CustomDialog(getActivity(), R.style.BrCustomDialog);
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_br_confirmation, null);
        dialog.setContentView(v);

        initViews(v);

        return dialog;
    }

    protected void setOnOkClickListener(View.OnClickListener onOkClickListener) {
        this.onOkClickListener = onOkClickListener;
    }

    private void initViews(View view) {
        final int type = getArguments().getInt(TYPE, TYPE_SUCCESS);

        ImageView icon = view.findViewById(R.id.confirmation_dialog_icon);
        MontserratTextView message = view.findViewById(R.id.confirmation_dialog_message);
        MontserratTextView okButton = view.findViewById(R.id.confirmation_dialog_ok_button);

        final boolean isTypeSuccess = type == TYPE_SUCCESS;

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTypeSuccess) {
                    ((DialogFragment) getParentFragment()).dismiss();
                }
                if (onOkClickListener != null) {
                    onOkClickListener.onClick(null);
                }
                dismiss();
            }
        });

        icon.setRotation(isTypeSuccess ? 0 : 180.0f);

        String text = getArguments().getString(MESSAGE, "");
        if (text.isEmpty()) {
            message.setText(getString(isTypeSuccess ? R.string.br_confirmation_success : R.string.br_confirmation_failure));
        } else {
            message.setText(text);
        }

        boolean containsLinks = getArguments().getBoolean(CONTAINS_LINKS, false);

        if (containsLinks) {
            Linkify.addLinks(message, Linkify.WEB_URLS);
        }
    }


}