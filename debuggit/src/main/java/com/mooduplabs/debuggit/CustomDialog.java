package com.mooduplabs.debuggit;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

public class CustomDialog extends Dialog {
    public CustomDialog(Context context, int theme) {
        super(context, theme);
        setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(0));
    }
}