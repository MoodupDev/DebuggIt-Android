package com.moodup.bugreporter.views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.moodup.bugreporter.R;

import butterknife.ButterKnife;

public class ReportFragment extends Fragment {

    public static final String TAG = ReportFragment.class.getSimpleName();

    private EditText title;
    private EditText content;
    private Button send;
    private ImageButton close;

    public static ReportFragment newInstance(String accessToken) {
        ReportFragment fragment = new ReportFragment();

        Bundle bundle = new Bundle();
        bundle.putString("accessToken", accessToken);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return initViews(inflater, container);
    }

    private View initViews(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_reporter, container, false);

        title = ButterKnife.findById(view, R.id.bug_title);
        content = ButterKnife.findById(view, R.id.bug_content);
        close = ButterKnife.findById(view, R.id.bug_close);
        send = ButterKnife.findById(view, R.id.bug_send_button);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo send the bug report to bitbucket
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });

        return view;
    }
}