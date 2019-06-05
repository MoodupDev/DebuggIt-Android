package com.mooduplabs.debuggit;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

public class MontserratEditText extends AppCompatEditText {

    private static final String REGULAR_FONT = "Montserrat-Regular.ttf";
    private static final String BOLD_FONT = "Montserrat-Bold.ttf";

    public MontserratEditText(Context context) {
        super(context);
    }

    public MontserratEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, 0);
    }

    public MontserratEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setCustomFont(context, defStyleAttr);
    }


    private void setCustomFont(Context context, int defStyleAttr) {
        String style = "fonts/";

        if (defStyleAttr == Typeface.BOLD) {
            style += BOLD_FONT;
        } else {
            style += REGULAR_FONT;
        }

        Typeface tf = Typeface.createFromAsset(context.getAssets(), style);

        if (tf != null) {
            this.setTypeface(tf);
        }
    }

}
