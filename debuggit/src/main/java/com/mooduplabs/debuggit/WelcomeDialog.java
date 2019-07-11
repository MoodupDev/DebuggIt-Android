package com.mooduplabs.debuggit;

import android.app.Dialog;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import androidx.fragment.app.DialogFragment;

public class WelcomeDialog extends DialogFragment implements View.OnClickListener {
    public static final String TAG = WelcomeDialog.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CustomDialog dialog = new CustomDialog(getActivity(), R.style.BrCustomDialog);
        final View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_br_welcome, null);

        dialog.setContentView(contentView);

        fixDialogDimensions(contentView);
        setCancelable(false);
        initViews(contentView);

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

    private void initViews(View view) {
        MontserratTextView letsGoButton = view.findViewById(R.id.welcome_lets_go_button);
        letsGoButton.setOnClickListener(this);
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
}
