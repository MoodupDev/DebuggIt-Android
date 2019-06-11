package com.mooduplabs.debuggit;

import android.app.Dialog;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.DialogFragment;

public class LoadingDialog extends DialogFragment {
    protected static final String TAG = LoadingDialog.class.getSimpleName();

    private View.OnClickListener onCancelClickListener;

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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CustomDialog dialog = new CustomDialog(getActivity(), R.style.BrCustomDialog);
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_br_loading, null);
        dialog.setContentView(v);

        if (dialog.getWindow() != null) {
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        initViews(v);

        return dialog;
    }

    protected void setOnCancelClickListener(View.OnClickListener onCancelClickListener) {
        this.onCancelClickListener = onCancelClickListener;
    }

    private void initViews(View view) {
        MontserratTextView message = view.findViewById(R.id.confirmation_dialog_message);
        MontserratTextView cancelButton = view.findViewById(R.id.loading_dialog_cancel_button);
        ProgressBar progressBar = view.findViewById(R.id.loading_spinner);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Drawable wrapDrawable = DrawableCompat.wrap(progressBar.getIndeterminateDrawable());
            DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(getContext(), R.color.br_app_orange));
            progressBar.setIndeterminateDrawable(DrawableCompat.unwrap(wrapDrawable));
        } else {
            progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.br_app_orange), PorterDuff.Mode.SRC_ATOP);
        }

        message.setText(getArguments().getString("message", ""));
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCancelClickListener != null) {
                    onCancelClickListener.onClick(v);
                }
                dismiss();
            }
        });
    }
}