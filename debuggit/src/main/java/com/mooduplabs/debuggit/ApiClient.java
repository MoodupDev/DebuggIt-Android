package com.mooduplabs.debuggit;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.HashMap;

class ApiClient {
    private static String uploadImageUrl;
    private static String uploadAudioUrl;

    protected static void setBaseUrl(String baseUrl) {
        ApiClient.uploadImageUrl = baseUrl + "/api/v1/upload/image";
        ApiClient.uploadAudioUrl = baseUrl + "/api/v1/upload/audio";
    }

    protected static void uploadImage(String imageData, String appId, JsonResponseCallback callback) {
        HashMap<String, String> data = new HashMap<>();
        data.put(Constants.Keys.DATA, imageData);
        data.put(Constants.Keys.APP_ID, appId);

        try {
            HttpClient.post(uploadImageUrl).withData(data).send(callback);
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            callback.onException(e);
        }
    }

    protected static void uploadAudio(String audioData, String appId, JsonResponseCallback callback) {
        HashMap<String, String> data = new HashMap<>();
        data.put(Constants.Keys.DATA, audioData);
        data.put(Constants.Keys.APP_ID, appId);

        try {
            HttpClient.post(uploadAudioUrl).withData(data).send(callback);
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            callback.onException(e);
        }
    }
}
