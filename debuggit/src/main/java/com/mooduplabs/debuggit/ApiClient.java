package com.mooduplabs.debuggit;

import android.content.Context;
import android.os.Build;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.HashMap;

class ApiClient {
    private static String uploadImageUrl;
    private static String uploadAudioUrl;
    private static String eventsUrl;

    protected static void setBaseUrl(String baseUrl) {
        ApiClient.uploadImageUrl = baseUrl + "/api/v1/upload/image";
        ApiClient.uploadAudioUrl = baseUrl + "/api/v1/upload/audio";
        ApiClient.eventsUrl = baseUrl + "/api/v2/events";
    }

    protected static void postEvent(Context context, EventType eventType) {
        postEvent(context, eventType, null);
    }

    protected static void postEvent(Context context, EventType eventType, Integer value) {
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.Keys.EVENT_TYPE, eventType.name().toLowerCase());
        params.put(Constants.Keys.APP_ID, context.getPackageName());
        params.put(Constants.Keys.SYSTEM_VERSION, String.valueOf(Build.VERSION.RELEASE));
        params.put(Constants.Keys.SYSTEM, Constants.Keys.ANDROID);
        params.put(Constants.Keys.DEVICE, Utils.getDeviceName());

        if (value != null) {
            params.put(Constants.Keys.VALUE, String.valueOf(value));
        }

        try {
            HttpClient.post(eventsUrl).withData(params).send();
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            e.printStackTrace();
        }
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

    protected enum EventType {
        INITIALIZED,
        SCREENSHOT_ADDED,
        SCREENSHOT_ADDED_RECTANGLE,
        SCREENSHOT_ADDED_DRAW,
        SCREENSHOT_REMOVED,
        SCREENSHOT_AMOUNT,
        AUDIO_ADDED,
        AUDIO_RECORD_TIME,
        AUDIO_PLAYED,
        AUDIO_REMOVED,
        AUDIO_AMOUNT,
        REPORT_SENT,
        REPORT_CANCELED,
        ACTUAL_BEHAVIOUR_FILLED,
        STEPS_TO_REPRODUCE_FILLED,
        EXPECTED_BEHAVIOUR_FILLED,
        APP_CRASHED
    }
}
