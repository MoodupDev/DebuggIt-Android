package com.mooduplabs.debuggit;

import android.content.Context;
import android.os.Build;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.HashMap;

public class ApiClient {

    public static final String UPLOAD_IMAGE_URL = BuildConfig.API_BASE_URL + "/api/v1/upload/image";
    public static final String UPLOAD_AUDIO_URL = BuildConfig.API_BASE_URL + "/api/v1/upload/audio";
    public static final String EVENTS_URL = BuildConfig.API_BASE_URL + "/api/v1/events";
    public static final String SUPPORTED_VERSION_URL = BuildConfig.API_BASE_URL + "/api/v1/supported_versions/%d";

    private String repoSlug;
    private String accountName;
    private String accessToken;

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

    public ApiClient(String repoSlug, String accountName, String accessToken) {
        this.repoSlug = repoSlug;
        this.accountName = accountName;
        this.accessToken = accessToken;
    }

    protected void addIssue(String title, String content, String priority, String kind, StringResponseCallback callback) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.Keys.TITLE, title);
        map.put(Constants.Keys.CONTENT, content);
        map.put(Constants.Keys.PRIORITY, priority);
        map.put(Constants.Keys.KIND, kind);

        try {
            HttpClient.post(String.format(Constants.BitBucket.ISSUES_URL, accountName, repoSlug)).withData(map).authUser(accessToken).send(callback);
        } catch(UnsupportedEncodingException | MalformedURLException e) {
            callback.onException(e);
        }
    }

    protected void refreshToken(String clientId, String clientSecret, String refreshToken, JsonResponseCallback callback) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.Keys.GRANT_TYPE, Constants.BitBucket.GRANT_TYPE_REFRESH_TOKEN);
        map.put(Constants.BitBucket.GRANT_TYPE_REFRESH_TOKEN, refreshToken);

        try {
            HttpClient.post(Constants.BitBucket.AUTHORIZE_URL).withData(map).authUser(clientId, clientSecret).send(callback);
        } catch(UnsupportedEncodingException | MalformedURLException e) {
            callback.onException(e);
        }
    }

    protected void login(String clientId, String clientSecret, String email, String password, JsonResponseCallback callback) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.Keys.GRANT_TYPE, Constants.BitBucket.GRANT_TYPE_PASSWORD);
        map.put(Constants.Keys.USERNAME, email);
        map.put(Constants.BitBucket.GRANT_TYPE_PASSWORD, password);

        try {
            HttpClient.post(Constants.BitBucket.AUTHORIZE_URL).withData(map).authUser(clientId, clientSecret).send(callback);
        } catch(UnsupportedEncodingException | MalformedURLException e) {
            callback.onException(e);
        }
    }

    protected static void checkVersion(int currentVersion, StringResponseCallback callback) {
        try {
            HttpClient.get(String.format(SUPPORTED_VERSION_URL, currentVersion)).send(callback);
        } catch(MalformedURLException e) {
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
        params.put(Constants.Keys.ANDROID_SDK, String.valueOf(Build.VERSION.SDK_INT));
        params.put(Constants.Keys.DEVICE, Utils.getDeviceName());
        if(value != null) params.put(Constants.Keys.VALUE, String.valueOf(value));

        try {
            HttpClient.post(EVENTS_URL).withData(params).send();
        } catch(UnsupportedEncodingException | MalformedURLException e) {
            e.printStackTrace();
        }
    }


    protected static void uploadImage(String imageData, String appId, JsonResponseCallback callback) {
        HashMap<String, String> data = new HashMap<>();
        data.put(Constants.Keys.DATA, imageData);
        data.put(Constants.Keys.APP_ID, appId);

        try {
            HttpClient.post(UPLOAD_IMAGE_URL).withData(data).send(callback);
        } catch(UnsupportedEncodingException | MalformedURLException e) {
            callback.onException(e);
        }
    }

    protected static void uploadAudio(String audioData, String appId, JsonResponseCallback callback) {
        HashMap<String, String> data = new HashMap<>();
        data.put(Constants.Keys.DATA, audioData);
        data.put(Constants.Keys.APP_ID, appId);

        try {
            HttpClient.post(UPLOAD_AUDIO_URL).withData(data).send(callback);
        } catch(UnsupportedEncodingException | MalformedURLException e) {
            callback.onException(e);
        }
    }
}
