package com.mooduplabs.debuggit;

import android.content.Context;
import android.os.Build;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.HashMap;

class ApiClient {
    public static final String UPLOAD_IMAGE_URL = BuildConfig.API_BASE_URL + "/api/v1/upload/image";
    public static final String UPLOAD_AUDIO_URL = BuildConfig.API_BASE_URL + "/api/v1/upload/audio";
    public static final String EVENTS_URL = BuildConfig.API_BASE_URL + "/api/v2/events";
    public static final String SUPPORTED_VERSION_URL = BuildConfig.API_BASE_URL + "/api/v2/supported_versions/android/%s";

    protected static void checkVersion(StringResponseCallback callback) {
        try {
            HttpClient.get(String.format(SUPPORTED_VERSION_URL, BuildConfig.VERSION_NAME)).send(callback);
        } catch (MalformedURLException e) {
            callback.onException(e);
        }
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
        if (value != null) params.put(Constants.Keys.VALUE, String.valueOf(value));

        try {
            HttpClient.post(EVENTS_URL).withData(params).send();
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    protected static void uploadImage(String imageData, String appId, JsonResponseCallback callback) {
        HashMap<String, String> data = new HashMap<>();
        data.put(Constants.Keys.DATA, imageData);
        data.put(Constants.Keys.APP_ID, appId);

        try {
            HttpClient.post(UPLOAD_IMAGE_URL).withData(data).send(callback);
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            callback.onException(e);
        }
    }

    protected static void uploadAudio(String audioData, String appId, JsonResponseCallback callback) {
        HashMap<String, String> data = new HashMap<>();
        data.put(Constants.Keys.DATA, audioData);
        data.put(Constants.Keys.APP_ID, appId);

        try {
            HttpClient.post(UPLOAD_AUDIO_URL).withData(data).send(callback);
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            callback.onException(e);
        }
    }

    protected enum EventType {
        INITIALIZED,
        HAS_UNSUPPORTED_VERSION,
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
