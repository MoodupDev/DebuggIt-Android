package com.mooduplabs.debuggit;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AudioCaptureFragment extends DialogFragment {
    public static final String TAG = AudioCaptureFragment.class.getSimpleName();
    public static final String TEMP_FILE_NAME = "recording";
    public static final int DURATION = 1000 * 60;
    public static final int COUNT_DOWN_INTERVAL = 1000;
    public static final String TIME_FORMAT = "%02d:%02d";

    private AudioCaptureHelper audioCaptureHelper;
    private CountDownTimer countDownTimer;
    private MontserratTextView timer;
    private AudioRecordListener listener;
    private LoadingDialog dialog;
    private ImageView recordDot;

    public interface AudioRecordListener {
        void onRecordUploaded(String audioUrl);

        void onFailed(boolean canceled);
    }

    public static AudioCaptureFragment newInstance(AudioRecordListener listener) {
        AudioCaptureFragment fragment = new AudioCaptureFragment();
        fragment.setListener(listener);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_br_report_audio_capture, container, false);
        initRecording();
        setCancelable(false);
        timer = (MontserratTextView) view.findViewById(R.id.timer_text);
        recordDot = (ImageView) view.findViewById(R.id.record_dot);

        ImageView recordButton = (ImageView) view.findViewById(R.id.record_btn);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.onFinish();
            }
        });

        dialog = LoadingDialog.newInstance(getString(R.string.br_loading_dialog_message_record), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                listener.onFailed(true);
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        if(countDownTimer != null) {
            countDownTimer = null;
        }
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

            private int recordingTime = 0;

            @Override
            public void onTick(long millisUntilFinished) {
                timer.setText(
                        String.format(Locale.getDefault(), TIME_FORMAT,
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished))
                );
                recordingTime += COUNT_DOWN_INTERVAL / 1000;
                recordDot.setVisibility(TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 2 == 0 ? View.INVISIBLE : View.VISIBLE);
            }

            @Override
            public void onFinish() {
                if(getActivity() != null && !isRemoving()) {
                    dialog.show(getChildFragmentManager(), LoadingDialog.TAG);
                    audioCaptureHelper.stopRecording();

                    ApiClient.postEvent(getContext(), ApiClient.EventType.AUDIO_RECORD_TIME, recordingTime);

                    ApiClient.uploadAudio(
                            Base64.encodeToString(Utils.getBytesFromFile(audioCaptureHelper.getFilePath()), Base64.NO_WRAP),
                            getContext().getPackageName(),
                            new JsonResponseCallback() {
                                @Override
                                public void onSuccess(JSONObject response) {
                                    if(isAdded()) {
                                        try {
                                            String url = response.getString("url");
                                            DebuggIt.getInstance().getReport().getAudioUrls().add(url);
                                            listener.onRecordUploaded(url);
                                            ApiClient.postEvent(getContext(), ApiClient.EventType.AUDIO_ADDED);
                                        } catch(JSONException e) {
                                            // ignored
                                        }
                                        dialog.dismiss();
                                        dismiss();
                                    }
                                }

                                @Override
                                public void onFailure(int responseCode, String errorMessage) {
                                    onUploadFailed();
                                }

                                private void onUploadFailed() {
                                    if(isAdded()) {
                                        dialog.dismiss();
                                        dismiss();
                                        listener.onFailed(false);
                                    }
                                }

                                @Override
                                public void onException(Exception ex) {
                                    onUploadFailed();
                                }
                            });
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
}

