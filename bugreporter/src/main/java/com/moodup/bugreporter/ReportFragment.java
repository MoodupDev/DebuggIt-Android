package com.moodup.bugreporter;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.cloudinary.utils.ObjectUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;

public class ReportFragment extends Fragment {

    protected static final String TAG = ReportFragment.class.getSimpleName();

    private ApiClient apiClient;
    private AudioCaptureHelper audioCaptureHelper;
    private ProgressDialog dialog;

    protected static ReportFragment newInstance() {
        ReportFragment fragment = new ReportFragment();

        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        apiClient = new ApiClient(
                BugReporter.getInstance().getRepoSlug(),
                BugReporter.getInstance().getAccountName(),
                BugReporter.getInstance().get
        );

        audioCaptureHelper = new AudioCaptureHelper();

        dialog = new ProgressDialog(getActivity());
        dialog.setTitle("Wait!");
        dialog.setMessage("you fool");

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
        Button record = ButterKnife.findById(view, R.id.record_button);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                apiClient.addIssue(
                        title.getText().toString(),
                        content.getText().toString()
                                + getUrlAsStrings(screensUrls, false)
                                + getUrlAsStrings(audioUrls, true),
                        new ApiClient.HttpHandler() {
                            @Override
                            public void done(HttpResponse data) {
                                dialog.hide();
                            }
                        }
                );
            }
        });

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                if (v.isSelected()) {
                    try {
                        audioCaptureHelper.startRecording(getActivity().getExternalCacheDir().getAbsolutePath() + "/recording.mpeg");
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                } else {
                    audioCaptureHelper.stopRecording();
                    try {
                        dialog.show();
                        new UploadAudioAsyncTask().execute(new FileInputStream(audioCaptureHelper.getFilePath()));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
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

    private String getUrlAsStrings(List<String> urls, boolean isMediaFile) {
        String urlsString = "";

        if (urls != null) {
            StringBuilder builder = new StringBuilder();
            for (String s : urls) {
                if (!isMediaFile) {
                    builder.append("![Alt text](");
                    builder.append(s);
                    builder.append(")");
                } else {
                    builder.append(s);
                }
            }

            urlsString = builder.toString();
        }

        return urlsString;
    }

    private class UploadAudioAsyncTask extends AsyncTask<InputStream, Void, List<String>> {
        @Override
        protected List<String> doInBackground(InputStream... params) {
            List<String> urls = new ArrayList<>();

            try {
                for (InputStream is : params) {
                    Map map = BugReporter.getInstance().getCloudinary().uploader().uploadLargeRaw(is, ObjectUtils.emptyMap());
                    urls.add(map.get("url") + Utils.MEDIA_FILE_FORMAT);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return urls;
        }

        @Override
        protected void onPostExecute(List<String> s) {
            dialog.hide();
            audioUrls.addAll(s);
            super.onPostExecute(s);
        }
    }
}
