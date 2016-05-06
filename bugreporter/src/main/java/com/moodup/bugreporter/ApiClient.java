package com.moodup.bugreporter;

import android.os.AsyncTask;

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

    private String urlString = "https://api.bitbucket.org/1.0/repositories/%1$s/%2$s/issues";

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
        HashMap<String, String> map = new HashMap<>();
        map.put("title", title);
        map.put("content", content);

        new AddIssueAsyncTask(map, handler).execute(String.format(urlString, accountName, repoSlug));
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
}
