package com.mooduplabs.debuggit;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Base64;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class ApiClient {

    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String PRIORITY = "priority";
    public static final String KIND = "kind";
    public static final String GRANT_TYPE = "grant_type";
    public static final String CLIENT_ID = "client_id";
    public static final String SECRET = "secret";
    public static final String METHOD_POST = "POST";
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    public static final String USERNAME = "username";
    public static final String NO_INTERNET_MESSAGE = "No internet connection";

    public static final String UPLOAD_IMAGE_URL = BuildConfig.API_BASE_URL + "/api/v1/upload/image";
    public static final String UPLOAD_AUDIO_URL = BuildConfig.API_BASE_URL + "/api/v1/upload/audio";
    public static final String EVENTS_URL = BuildConfig.API_BASE_URL + "/api/v1/events";
    public static final String SUPPORTED_VERSION_URL = BuildConfig.API_BASE_URL + "/api/v1/supported_versions/%d";

    public static final int TIMEOUT_MILLIS = 15000;
    public static final int NO_CONNECTION_RESPONSE_CODE = -1;

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

    protected interface HttpHandler {
        void done(HttpResponse data);
    }

    public ApiClient(String repoSlug, String accountName, String accessToken) {
        this.repoSlug = repoSlug;
        this.accountName = accountName;
        this.accessToken = accessToken;
    }

    protected void addIssue(String title, String content, String priority, String kind, HttpHandler handler) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TITLE, title);
        map.put(CONTENT, content);
        map.put(PRIORITY, priority);
        map.put(KIND, kind);

        new AddIssueAsyncTask(map, handler).execute(String.format(BitBucket.ISSUES_URL, accountName, repoSlug));
    }

    protected void refreshToken(String clientId, String clientSecret, String refreshToken, HttpHandler handler) {
        HashMap<String, String> map = new HashMap<>();
        map.put(GRANT_TYPE, BitBucket.GRANT_TYPE_REFRESH_TOKEN);
        map.put(CLIENT_ID, clientId);
        map.put(SECRET, clientSecret);
        map.put(BitBucket.GRANT_TYPE_REFRESH_TOKEN, refreshToken);

        new ApiClient.AuthorizeAsyncTask(map, handler).execute(BitBucket.AUTHORIZE_URL);
    }

    protected void login(String clientId, String clientSecret, String email, String password, HttpHandler handler) {
        HashMap<String, String> map = new HashMap<>();
        map.put(GRANT_TYPE, BitBucket.GRANT_TYPE_PASSWORD);
        map.put(CLIENT_ID, clientId);
        map.put(SECRET, clientSecret);
        map.put(USERNAME, email);
        map.put(BitBucket.GRANT_TYPE_PASSWORD, password);

        new ApiClient.AuthorizeAsyncTask(map, handler).execute(BitBucket.AUTHORIZE_URL);
    }

    protected static void checkVersion(int currentVersion, HttpHandler handler) {
        new CheckSupportedVersion(handler).execute(String.format(Locale.getDefault(), SUPPORTED_VERSION_URL, currentVersion));
    }

    protected static void postEvent(Context context, EventType eventType) {
        postEvent(context, eventType, null);
    }

    protected static void postEvent(Context context, EventType eventType, Integer value) {
        HashMap<String, String> params = new HashMap<>();
        params.put("event_type", eventType.name().toLowerCase());
        params.put("app_id", context.getPackageName());
        params.put("android_sdk", String.valueOf(Build.VERSION.SDK_INT));
        params.put("device", Utils.getDeviceName());
        if(value != null) params.put("value", String.valueOf(value));

        new PostEventAsyncTask(params).execute(EVENTS_URL);
    }

    protected class AuthorizeAsyncTask extends AsyncTask<String, Void, HttpResponse> {

        private HashMap<String, String> postParams;
        private HttpHandler handler;

        public AuthorizeAsyncTask(HashMap<String, String> postParams, HttpHandler handler) {
            this.postParams = postParams;
            this.handler = handler;
        }

        @Override
        protected HttpResponse doInBackground(String... params) {
            return getHttpResponse(params, postParams, true);
        }

        @Override
        protected void onPostExecute(HttpResponse httpResponse) {
            handler.done(httpResponse);
            super.onPostExecute(httpResponse);
        }
    }

    protected class AddIssueAsyncTask extends AsyncTask<String, Void, HttpResponse> {

        private HashMap<String, String> postParams;
        private HttpHandler handler;

        public AddIssueAsyncTask(HashMap<String, String> postParams, HttpHandler handler) {
            this.handler = handler;
            this.postParams = postParams;
        }

        @Override
        protected HttpResponse doInBackground(String... params) {
            return getHttpResponse(params, postParams, false);
        }


        @Override
        protected void onPostExecute(HttpResponse result) {
            handler.done(result);
            super.onPostExecute(result);
        }
    }

    protected static class CheckSupportedVersion extends AsyncTask<String, Void, HttpResponse> {
        private HttpHandler handler;

        public CheckSupportedVersion(HttpHandler handler) {
            this.handler = handler;
        }

        @Override
        protected HttpResponse doInBackground(String... params) {
            URL url;
            try {
                url = new URL(params[0]);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(TIMEOUT_MILLIS);
                conn.setConnectTimeout(TIMEOUT_MILLIS);
                conn.setDoOutput(true);

                int response = conn.getResponseCode();
                if(response == HttpsURLConnection.HTTP_OK) {
                    return new HttpResponse(response, Utils.getStringFromInputStream(conn.getInputStream()));
                }
                return new HttpResponse(response, Utils.getStringFromInputStream(conn.getErrorStream()));
            } catch(Exception e) {
                e.printStackTrace();
            }
            return new HttpResponse(NO_CONNECTION_RESPONSE_CODE, NO_INTERNET_MESSAGE);
        }

        @Override
        protected void onPostExecute(HttpResponse httpResponse) {
            handler.done(httpResponse);
            super.onPostExecute(httpResponse);
        }
    }

    protected static class PostEventAsyncTask extends AsyncTask<String, Void, Void> {

        private HashMap<String, String> postParams;

        public PostEventAsyncTask(HashMap<String, String> postParams) {
            this.postParams = postParams;
        }

        @Override
        protected Void doInBackground(String... params) {
            URL url;
            try {
                url = new URL(params[0]);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(TIMEOUT_MILLIS);
                conn.setConnectTimeout(TIMEOUT_MILLIS);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestMethod("POST");

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(Utils.getPostDataString(postParams));

                writer.flush();
                writer.close();
                os.close();
                conn.getResponseCode();
            } catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private HttpResponse getHttpResponse(String[] params, HashMap<String, String> postParams, boolean authorization) {
        URL url;
        try {
            url = new URL(params[0]);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIMEOUT_MILLIS);
            conn.setConnectTimeout(TIMEOUT_MILLIS);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.addRequestProperty("Authorization",
                    authorization ?
                            "Basic " + Base64.encodeToString((postParams.get(CLIENT_ID) + ":" + postParams.get(SECRET)).getBytes(), Base64.DEFAULT)
                            : "Bearer " + URLDecoder.decode(accessToken, CHARSET_UTF8));
            conn.addRequestProperty(CONTENT_TYPE, CONTENT_TYPE_FORM); //add the content type of the request, most post data is of this type

            conn.setRequestMethod(METHOD_POST);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, CHARSET_UTF8));
            writer.write(Utils.getPostDataString(postParams));

            writer.flush();
            writer.close();
            os.close();
            int response = conn.getResponseCode();

            if(response == HttpsURLConnection.HTTP_OK) {
                return new HttpResponse(response, Utils.getStringFromInputStream(conn.getInputStream()));
            } else {
                if(response == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                    DebuggIt.getInstance().authenticate(true);
                }
                return new HttpResponse(response, Utils.getStringFromInputStream(conn.getErrorStream()));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return new HttpResponse(NO_CONNECTION_RESPONSE_CODE, NO_INTERNET_MESSAGE);
    }

    protected static String getUploadedFileUrl(HashMap<String, String> postParams, boolean isImage) {
        URL url;
        try {
            url = new URL(isImage ? UPLOAD_IMAGE_URL : UPLOAD_AUDIO_URL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIMEOUT_MILLIS);
            conn.setConnectTimeout(TIMEOUT_MILLIS);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(Utils.getPostDataString(postParams));

            writer.flush();
            writer.close();
            os.close();
            int response = conn.getResponseCode();
            if(response == HttpsURLConnection.HTTP_OK) {
                JSONObject json = new JSONObject(Utils.getStringFromInputStream(conn.getInputStream()));
                return json.getString("url");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
