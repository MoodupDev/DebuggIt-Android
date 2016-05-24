package com.moodup.bugreporter;

import android.os.AsyncTask;
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

    protected void addIssue(String title, String content, HttpHandler handler) {
        addIssue(title, content, BitBucket.PRIORITY_TRIVIAL, BitBucket.KIND_BUG, handler);
    }

    protected void addIssue(String title, String content, String priority, HttpHandler handler) {
        addIssue(title, content, priority, BitBucket.KIND_BUG, handler);
    }

    protected void addIssue(String title, String content, String priority, String kind, HttpHandler handler) {
        HashMap<String, String> map = new HashMap<>();
        map.put("title", title);
        map.put("content", content);
        map.put("priority", priority);
        map.put("kind", kind);

        new AddIssueAsyncTask(map, handler).execute(String.format(BitBucket.ISSUES_URL, accountName, repoSlug));
    }

    protected void authorize(String token, String clientId, String secret, boolean refresh, HttpHandler handler) {
        HashMap<String, String> map = new HashMap<>();
        map.put("grant_type", refresh ? "refresh_token" : "authorization_code");
        map.put("client_id", clientId);
        map.put("secret", secret);
        map.put(refresh ? "refresh_token" : "code", token);

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
            URL url;
            InputStream is;
            try {
                url = new URL(params[0]);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
//                conn.addRequestProperty("Authorization", "Bearer " + URLDecoder.decode(accessToken, "UTF-8"));
                conn.setRequestProperty("Authorization", "Basic " + Base64.encodeToString((postParams.get("client_id") + ":" + postParams.get("secret")).getBytes(), Base64.DEFAULT));
                conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded"); //add the content type of the request, most post data is of this type

                conn.setRequestMethod("POST");

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postParams));

                writer.flush();
                writer.close();
                os.close();
                int response = conn.getResponseCode();

                if (response == HttpsURLConnection.HTTP_OK) {
                    is = conn.getInputStream();
                    return new HttpResponse(response, Utils.getStringFromInputStream(is));
                } else if (response == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                    BugReporter.getInstance().authenticate(true);
                    return new HttpResponse(response, "");
                } else {
                    return new HttpResponse(response, "");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new HttpResponse();
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
            URL url;
            InputStream is;
            try {
                url = new URL(params[0]);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.addRequestProperty("Authorization", "Bearer " + URLDecoder.decode(accessToken, "UTF-8"));
                conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded"); //add the content type of the request, most post data is of this type

                conn.setRequestMethod("POST");

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postParams));

                writer.flush();
                writer.close();
                os.close();
                int response = conn.getResponseCode();

                if (response == HttpsURLConnection.HTTP_OK) {
                    is = conn.getInputStream();
                    return new HttpResponse(response, Utils.getStringFromInputStream(is));
                } else if (response == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                    BugReporter.getInstance().authenticate(true);
                    return new HttpResponse(response, "");
                } else {
                    return new HttpResponse(response, "");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new HttpResponse();
        }

        @Override
        protected void onPostExecute(HttpResponse result) {
            handler.done(result);
            super.onPostExecute(result);
        }
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
