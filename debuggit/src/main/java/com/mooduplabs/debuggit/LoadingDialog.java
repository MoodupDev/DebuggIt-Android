package com.mooduplabs.debuggit;

import android.app.Dialog;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
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
        final View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_br_loading, null);

        dialog.setContentView(contentView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        fixDialogDimensions(contentView);
        initViews(contentView);

        return dialog;
    }

    protected void setOnCancelClickListener(View.OnClickListener onCancelClickListener) {
        this.onCancelClickListener = onCancelClickListener;
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