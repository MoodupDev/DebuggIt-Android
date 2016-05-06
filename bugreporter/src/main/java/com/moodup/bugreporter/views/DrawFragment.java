package com.moodup.bugreporter.views;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.moodup.bugreporter.BugReporter;
import com.moodup.bugreporter.R;

import butterknife.ButterKnife;

public class DrawFragment extends Fragment {

    public static final String TAG = DrawFragment.class.getSimpleName();

    private ImageView surface;
    private Button cancel;
    private Button confirm;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return initViews(inflater, container);
    }

    private View initViews(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_draw, container, false);

        surface = ButterKnife.findById(view, R.id.draw_surface);
        cancel = ButterKnife.findById(view, R.id.draw_cancel);
        confirm = ButterKnife.findById(view, R.id.draw_confirm);

        initDrawingSurface();
        initButtons();

        return view;
    }

    private void initDrawingSurface() {
        View rootView = getActivity().findViewById(android.R.id.content);

        rootView.setDrawingCacheEnabled(true);

        surface.setImageBitmap(Bitmap.createBitmap(rootView.getDrawingCache()));

        rootView.setDrawingCacheEnabled(false);
        rootView.destroyDrawingCache();
    }

    private void initButtons() {
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStackImmediate();
                BugReporter.getInstance().showReportFragment();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });
    }
}
