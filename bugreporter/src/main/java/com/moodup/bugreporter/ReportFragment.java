package com.moodup.bugreporter;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;

import butterknife.ButterKnife;

public class ReportFragment extends Fragment {

    protected static final String TAG = ReportFragment.class.getSimpleName();

    private ApiClient apiClient;

    protected static ReportFragment newInstance(String accessToken, ArrayList<String> urls) {
        ReportFragment fragment = new ReportFragment();

        Bundle bundle = new Bundle();
        bundle.putString("accessToken", accessToken);
        bundle.putStringArrayList("urls", urls);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        apiClient = new ApiClient(
                BugReporter.getInstance().getRepoSlug(),
                BugReporter.getInstance().getAccountName(),
                getArguments().getString("accessToken", "")
        );

        return initViews(inflater, container);
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    private View initViews(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_reporter, container, false);

        final EditText title = ButterKnife.findById(view, R.id.bug_title);
        final EditText content = ButterKnife.findById(view, R.id.bug_content);

        ImageButton close = ButterKnife.findById(view, R.id.bug_close);
        Button send = ButterKnife.findById(view, R.id.bug_send_button);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setTitle("Wait!");
                dialog.setMessage("you fool");

                dialog.show();
                apiClient.addIssue(
                        title.getText().toString(),
                        content.getText().toString() + getUrlAsStrings(),
                        new ApiClient.HttpHandler() {
                            @Override
                            public void done(HttpResponse data) {
                                dialog.hide();
                            }
                        }
                );
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


    private String getUrlAsStrings() {
        String screens = "";
        ArrayList<String> urls = getArguments().getStringArrayList("urls");

        if (urls != null) {
            StringBuilder builder = new StringBuilder();
            for (String s : urls) {
                builder.append("![Alt text](");
                builder.append(s);
                builder.append(")");
            }

            screens = builder.toString();
        }

        return screens;
    }

}
