package com.moodup.bugreporter;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.cloudinary.utils.ObjectUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;

public class AudioCaptureFragment extends DialogFragment {
    public static final String TAG = AudioCaptureFragment.class.getSimpleName();
    public static final String TEMP_FILE_NAME = "recording";
    public static final int DURATION = 1000 * 60;
    public static final int COUNT_DOWN_INTERVAL = 1000;

    private AudioCaptureHelper audioCaptureHelper;
    private CountDownTimer countDownTimer;
    private MontserratTextView timer;
    private AudioRecordListener listener;
    private UploadAudioAsyncTask uploadAudioAsyncTask;
    private LoadingDialog dialog;

    public interface AudioRecordListener {
        void onRecordUploaded(String audioUrl);

        void onFailed();
    }

    public static AudioCaptureFragment newInstance(AudioRecordListener listener) {
        AudioCaptureFragment fragment = new AudioCaptureFragment();
        fragment.setListener(listener);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio_capture, container, false);
        initRecording();
        setCancelable(false);
        timer = ButterKnife.findById(view, R.id.timer_text);

        ImageView recordButton = ButterKnife.findById(view, R.id.record_btn);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.onFinish();
            }
        });

        dialog = LoadingDialog.newInstance(getString(R.string.loading_dialog_message_record));

        return view;
    }

    @Override
    public void onDestroyView() {
        if (countDownTimer != null) {
            countDownTimer = null;
        }

        if (uploadAudioAsyncTask != null) {
            uploadAudioAsyncTask.cancel(true);
        }
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    private void initRecording() {
        String filePath = getActivity().getFilesDir() + TEMP_FILE_NAME + Utils.MEDIA_FILE_FORMAT;

        audioCaptureHelper = new AudioCaptureHelper();
        audioCaptureHelper.startRecording(filePath);

        initTimer();
    }

    private void initTimer() {
        countDownTimer = new CountDownTimer(DURATION, COUNT_DOWN_INTERVAL) {

            @Override
            public void onTick(long millisUntilFinished) {
                timer.setText(
                        String.format("%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished))
                );
                // TODO: 11.05.2016 blink red dot
            }

            @Override
            public void onFinish() {
                if (getActivity() != null && !isRemoving()) {
                    dialog.show(getChildFragmentManager(), LoadingDialog.TAG);
                    audioCaptureHelper.stopRecording();
                    try {
                        uploadAudioAsyncTask = new UploadAudioAsyncTask();
                        uploadAudioAsyncTask.execute(new FileInputStream(audioCaptureHelper.getFilePath()));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    @Override
    public void onDetach() {
        audioCaptureHelper.stopRecording();
        super.onDetach();
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }

    public void setListener(AudioRecordListener listener) {
        this.listener = listener;
    }

    private class UploadAudioAsyncTask extends AsyncTask<InputStream, Void, List<String>> {
        @Override
        protected List<String> doInBackground(InputStream... params) {
            List<String> urls = new ArrayList<>();

            try {
                for (InputStream is : params) {
                    Map map = BugReporter.getInstance().getCloudinary().uploader().uploadLargeRaw(is, ObjectUtils.asMap("resource_type", "video"));
                    urls.add((String) map.get("url"));
                }
            } catch (IOException e) {
                e.printStackTrace();
                listener.onFailed();
            }

            return urls;
        }

        @Override
        protected void onPostExecute(List<String> s) {
            dialog.dismiss();
            BugReporter.getInstance().getReport().getAudioUrls().addAll(s);
            listener.onRecordUploaded(s.get(0));
            dismiss();
            super.onPostExecute(s);
        }
    }
}

