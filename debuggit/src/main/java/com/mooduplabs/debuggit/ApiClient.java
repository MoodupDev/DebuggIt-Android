package com.mooduplabs.debuggit;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.HashMap;

class ApiClient {
    private static ApiInterface apiInterface;

    private static Boolean defaultApiClientConfigured = false;
    private static Boolean customApiClientConfigured = false;

    protected static void initApi(final String baseUrl, final String uploadImageEndpoint, final String uploadAudioEndpoint) {
        ApiClient.apiInterface = new ApiInterface() {
            private String uploadImageUrl = baseUrl + uploadImageEndpoint;
            private String uploadAudioUrl = baseUrl + uploadAudioEndpoint;

            @Override
            public void uploadImage(String imageData, JsonResponseCallback callback) {
                HashMap<String, String> data = new HashMap<>();
                data.put(Constants.Keys.DATA, imageData);

                try {
                    HttpClient.post(uploadImageUrl).withData(data).send(callback);
                } catch (UnsupportedEncodingException | MalformedURLException e) {
                    callback.onException(e);
                }
            }

            @Override
            public void uploadAudio(String audioData, JsonResponseCallback callback) {
                HashMap<String, String> data = new HashMap<>();
                data.put(Constants.Keys.DATA, audioData);

                try {
                    HttpClient.post(uploadAudioUrl).withData(data).send(callback);
                } catch (UnsupportedEncodingException | MalformedURLException e) {
                    callback.onException(e);
                }
            }
        };

        ApiClient.defaultApiClientConfigured = true;
    }

    protected static void initCustomApi(ApiInterface customApiInterface) {
        ApiClient.apiInterface = customApiInterface;
        ApiClient.customApiClientConfigured = true;
    }

    protected static Boolean isDefaultApiClientConfigured() {
        return ApiClient.defaultApiClientConfigured;
    }

    protected static Boolean isCustomApiClientConfigured() {
        return ApiClient.customApiClientConfigured;
    }

    protected static void uploadImage(String imageData, JsonResponseCallback callback) {
        ApiClient.apiInterface.uploadImage(imageData, callback);
    }

    protected static void uploadAudio(String audioData, JsonResponseCallback callback) {
        ApiClient.apiInterface.uploadAudio(audioData, callback);
    }
}
