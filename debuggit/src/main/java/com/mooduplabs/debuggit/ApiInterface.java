package com.mooduplabs.debuggit;

public interface ApiInterface {
    void uploadImage(String imageData, String appId, JsonResponseCallback callback);

    void uploadAudio(String audioData, String appId, JsonResponseCallback callback);
}
