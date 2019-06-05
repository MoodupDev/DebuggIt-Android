package com.mooduplabs.debuggit;

import android.media.MediaRecorder;

import java.io.IOException;

public class AudioCaptureHelper {
    private static final int SAMPLING_RATE = 44100;
    private static final int BIT_RATE = 96000;

    private MediaRecorder mediaRecorder = null;
    private String filePath = null;
    private boolean isRecording = false;

    protected AudioCaptureHelper() {
    }

    protected void startRecording(String filePath) {
        this.filePath = filePath;
        isRecording = true;
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioSamplingRate(SAMPLING_RATE);
        mediaRecorder.setAudioEncodingBitRate(BIT_RATE);
        mediaRecorder.setOutputFile(filePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            // ignored
        }
        mediaRecorder.start();
    }

    protected void stopRecording() {
        if (isRecording) {
            isRecording = false;
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    protected String getFilePath() {
        return filePath;
    }
}