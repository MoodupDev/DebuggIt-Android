package com.mooduplabs.debuggit;

import android.media.MediaRecorder;

import java.io.IOException;

public class AudioCaptureHelper {

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
            // ignored
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