package com.moodup.bugreporter;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Base64;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

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

    private String repoSlug;
    private String accountName;
    private String accessToken;

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

    @NonNull
    private HttpResponse getHttpResponse(String[] params, HashMap<String, String> postParams, boolean authorization) {
        URL url;
        InputStream is;
        try {
            url = new URL(params[0]);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
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
            writer.write(getPostDataString(postParams));

            writer.flush();
            writer.close();
            os.close();
            int response = conn.getResponseCode();

            if(response == HttpsURLConnection.HTTP_OK) {
                is = conn.getInputStream();
                return new HttpResponse(response, Utils.getStringFromInputStream(is));
            } else if(response == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                BugReporter.getInstance().authenticate(true);
                return new HttpResponse(response, "");
            } else if(response == HttpURLConnection.HTTP_FORBIDDEN) {
                return new HttpResponse(response, Utils.getStringFromInputStream(conn.getErrorStream()));
            } else {
                return new HttpResponse(response, "");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return new HttpResponse(-1, NO_INTERNET_MESSAGE);
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()) {
            if(first) {
                first = false;
            } else {
                result.append("&");
            }

            result.append(URLEncoder.encode(entry.getKey(), CHARSET_UTF8))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), CHARSET_UTF8));
        }

        return result.toString();
    }
}
