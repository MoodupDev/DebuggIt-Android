package com.mooduplabs.debuggit;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class MontserratTextView extends TextView {

    private static final String REGULAR_FONT = "Montserrat-Regular.ttf";
    private static final String BOLD_FONT = "Montserrat-Bold.ttf";

    public MontserratTextView(Context context) {
        super(context);
    }

    public MontserratTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, 0);
    }

    public MontserratTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setCustomFont(context, defStyleAttr);
    }


    private void setCustomFont(Context context, int defStyleAttr) {
        String style = "fonts/";
        switch(defStyleAttr) {
            case Typeface.BOLD:
                style += BOLD_FONT;
                break;
            default:
                style += REGULAR_FONT;
                break;
        }
        Typeface tf = Typeface.createFromAsset(context.getAssets(), style);
        if(tf != null) {
            this.setTypeface(tf);
        }
    }

}
