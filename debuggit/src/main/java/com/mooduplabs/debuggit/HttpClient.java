package com.mooduplabs.debuggit;

import android.os.AsyncTask;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

class HttpClient {
    public static final String ACCEPT_HEADER = "Accept";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String CHARSET_UTF8 = "UTF-8";
    private static final int DEFAULT_TIMEOUT_MILLIS = 15000;
    private static final String CONTENT_TYPE_KEY = "Content-type";
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private URL url;
    private HttpURLConnection connection;
    private OutputStream os;
    private HashMap<String, String> headers;
    private Method method;
    private String data;
    private String response;
    private Exception exception;
    private int responseCode;
    private int timeout;

    private HttpClient(String url, Method method) throws MalformedURLException {
        this.url = new URL(url);
        this.method = method;
        this.headers = new HashMap<>();
        this.timeout = DEFAULT_TIMEOUT_MILLIS;
    }

    public static HttpClient get(String url) throws MalformedURLException {
        return new HttpClient(url, Method.GET);
    }

    public static HttpClient post(String url) throws MalformedURLException {
        return new HttpClient(url, Method.POST);
    }

    public static HttpClient delete(String url) throws MalformedURLException {
        return new HttpClient(url, Method.DELETE);
    }

    public static HttpClient put(String url) throws MalformedURLException {
        return new HttpClient(url, Method.PUT);
    }

    public HttpClient withData(HashMap<String, String> data) throws UnsupportedEncodingException {
        this.data = getPostDataString(data);
        return this;
    }

    public HttpClient withData(String data) {
        this.data = data;
        if (isDataJson(data)) {
            setContentTypeToJson();
        }
        return this;
    }

    private boolean isDataJson(String data) {
        try {
            JSONObject object = new JSONObject(data);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public HttpClient withData(JSONObject object) {
        setContentTypeToJson();
        this.data = object.toString();
        return this;
    }

    public HttpClient withTimeout(int timeout) {
        this.timeout = timeout < 1000 ? DEFAULT_TIMEOUT_MILLIS : timeout;
        return this;
    }

    private void setContentTypeToJson() {
        this.headers.put(CONTENT_TYPE_KEY, CONTENT_TYPE_APPLICATION_JSON);
    }

    public HttpClient withHeaders(HashMap<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    public HttpClient withHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public HttpClient authUser(String username, String password) {
        withHeader(AUTHORIZATION_HEADER, getBasicAuthString(username, password));
        return this;
    }

    public HttpClient authUser(String token) {
        withHeader(AUTHORIZATION_HEADER, getBearerAuthString(token));
        return this;
    }

    private String getBearerAuthString(String token) {
        return String.format("Bearer %s", token);
    }

    public void send() {
        send(new StringResponseCallback() {
            @Override
            public void onSuccess(String response) {

            }

            @Override
            public void onFailure(int responseCode, String errorMessage) {

            }

            @Override
            public void onException(Exception e) {

            }
        });
    }

    public void send(final StringResponseCallback callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                getResponse();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (exception != null) {
                    callback.onException(exception);
                } else {
                    if (isSuccessful()) {
                        callback.onSuccess(response);
                    } else {
                        callback.onFailure(responseCode, response);
                    }
                }
                connection.disconnect();
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    public void send(final JsonResponseCallback callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                getResponse();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (exception != null) {
                    callback.onException(exception);
                } else {
                    if (isSuccessful()) {
                        try {
                            callback.onSuccess(new JSONObject(response));
                        } catch (JSONException e) {
                            callback.onException(e);
                        }
                    } else {
                        callback.onFailure(responseCode, response);
                    }
                }
                connection.disconnect();
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    private void getResponse() {
        try {
            connect();
            readResponse();
        } catch (IOException e) {
            exception = e;
        }
    }

    private void readResponse() throws IOException {
        if (isSuccessful()) {
            try {
                response = readStringResponse(connection.getInputStream());
            } catch (IOException e) {
                exception = e;
            }
        } else {
            try {
                response = readStringResponse(connection.getErrorStream());
            } catch (Exception e) {
                exception = e;
            }
        }
    }

    private void connect() throws IOException {
        openConnection();
        setHeaders();
        initConnectionConfig();
        writeToOutputStream(data);
        responseCode = connection.getResponseCode();
    }

    private void setHeaders() {
        for (Map.Entry<String, String> header : headers.entrySet()) {
            connection.addRequestProperty(header.getKey(), header.getValue());
        }
    }

    private void openConnection() throws IOException {
        connection = (HttpURLConnection) this.url.openConnection();
    }

    private String readStringResponse(InputStream stream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuilder response = new StringBuilder();
        for (String line; (line = br.readLine()) != null; ) response.append(line).append("\n");
        br.close();
        return response.toString();
    }

    private JSONObject readJsonResponse(InputStream stream) throws IOException, JSONException {
        return new JSONObject(readStringResponse(stream));
    }

    private void initConnectionConfig() throws IOException {
        connection.setDoInput(true);
        connection.setRequestMethod(method.name());
        connection.setReadTimeout(timeout);
        connection.setConnectTimeout(timeout);
        if (method == Method.POST || method == Method.PUT) {
            connection.setDoOutput(true);
            os = connection.getOutputStream();
        }
    }

    private boolean isSuccessful() {
        return responseCode >= 200 && responseCode < 300;
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
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

    private String getBasicAuthString(String username, String password) {
        return String.format("Basic %s", Base64.encodeToString(String.format("%s:%s", username, password).getBytes(), Base64.DEFAULT));
    }

    private void writeToOutputStream(String data) {
        if ((method == Method.POST || method == Method.PUT) && data != null) {
            BufferedWriter writer;
            try {
                writer = new BufferedWriter(new OutputStreamWriter(os, CHARSET_UTF8));
                writer.write(data);
                writer.flush();
                writer.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private enum Method {
        POST, PUT, DELETE, GET
    }
}
