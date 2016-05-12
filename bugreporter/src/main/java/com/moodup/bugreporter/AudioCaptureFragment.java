package com.moodup.bugreporter;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    /*v.setSelected(!v.isSelected());
    if (v.isSelected()) {
        try {
            audioCaptureHelper.startRecording(getActivity().getExternalCacheDir().getAbsolutePath() + "/recording.mpeg");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    } else {
        audioCaptureHelper.stopRecording();
        try {
            dialog.show(getChildFragmentManager(), LoadingDialog.TAG);
            uploadAudioAsyncTask = new UploadAudioAsyncTask();
            uploadAudioAsyncTask.execute(new FileInputStream(audioCaptureHelper.getFilePath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio_capture, container, false);
        initRecording();
        setCancelable(false);
        timer = ButterKnife.findById(view, R.id.timer_text);
        return view;
    }

    private void initRecording() {
        audioCaptureHelper = new AudioCaptureHelper();
        String filePath = getActivity().getFilesDir() + TEMP_FILE_NAME + Utils.MEDIA_FILE_FORMAT;
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
                // TODO: 11.05.2016 finish recording, upload file
            }
        }.start();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        audioCaptureHelper.stopRecording();
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }
}

