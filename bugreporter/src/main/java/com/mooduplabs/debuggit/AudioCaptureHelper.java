package com.mooduplabs.debuggit;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

public class AudioCaptureHelper {

    private static final String LOG_TAG = "AudioRecordTest";
    private MediaRecorder mediaRecorder = null;
    private String filePath = null;
    private boolean recording = false;
    private boolean recordingStarted = false;

    protected AudioCaptureHelper() {
    }


    protected void startRecording(String filePath) {
        this.filePath = filePath;
        recordingStarted = true;
        recording = true;
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setAudioEncodingBitRate(96000);
        mediaRecorder.setOutputFile(filePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        mediaRecorder.start();
    }

    protected void stopRecording() {
        if (recording) {
            recording = false;
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    protected boolean isRecording() {
        return recording;
    }

    protected boolean hasRecordingStarted() {
        return recordingStarted;
    }

    protected String getFilePath() {
        return filePath;
    }
}