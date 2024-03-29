package com.mooduplabs.debuggit;

import android.app.Dialog;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
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
        final View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_br_confirmation, null);

        dialog.setContentView(contentView);

        dialog.setCanceledOnTouchOutside(false);
        fixDialogDimensions(contentView);
        initViews(contentView);

        return dialog;
    }

    protected void setOnOkClickListener(View.OnClickListener onOkClickListener) {
        this.onOkClickListener = onOkClickListener;
    }

    private void fixDialogDimensions(final View contentView) {
        ViewTreeObserver vto = contentView.getViewTreeObserver();

        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int displayWidth = -1;
            int displayHeight = -1;
            DisplayMetrics metrics = null;
            View containerView = null;

            @Override
            public void onGlobalLayout() {
                if (containerView == null) {
                    ViewParent viewParent = contentView.getParent();
                    containerView = contentView;

                    while (viewParent instanceof View) {
                        containerView = (View) viewParent;
                        viewParent = viewParent.getParent();
                    }
                }

                if (metrics == null) {
                    metrics = new DisplayMetrics();
                }

                if (getActivity() != null) {
                    Display display = getActivity().getWindowManager().getDefaultDisplay();

                    if (displayHeight == -1) {
                        display.getMetrics(metrics);

                        WindowManager.LayoutParams params = (WindowManager.LayoutParams) containerView.getLayoutParams();

                        displayHeight = metrics.heightPixels;
                        params.height = displayHeight;

                        containerView.setLayoutParams(params);
                        getActivity().getWindowManager().updateViewLayout(containerView, params);
                    }

                    if (displayWidth == -1) {
                        int[] attrs = {android.R.attr.minWidth};
                        TypedArray typedArray = getActivity().obtainStyledAttributes(R.style.BrCustomDialog, attrs);
                        int minDialogWidth = (int) typedArray.getDimension(0, getResources().getDimension(R.dimen.br_confirmation_dialog_width));
                        typedArray.recycle();

                        WindowManager.LayoutParams params = (WindowManager.LayoutParams) containerView.getLayoutParams();

                        displayWidth = minDialogWidth;
                        params.width = displayWidth;

                        containerView.setLayoutParams(params);
                        getActivity().getWindowManager().updateViewLayout(containerView, params);
                    }
                }
            }
        });
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