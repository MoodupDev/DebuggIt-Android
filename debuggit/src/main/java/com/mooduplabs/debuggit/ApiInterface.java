package com.mooduplabs.debuggit;

public interface ApiInterface {
    void uploadImage(String imageData, JsonResponseCallback callback);

    void uploadAudio(String audioData, JsonResponseCallback callback);
}
