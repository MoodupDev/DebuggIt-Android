package com.moodup.bugreporter;

import javax.net.ssl.HttpsURLConnection;

public class HttpResponse {
    private String message;
    private int responseCode;

    protected HttpResponse() {
    }

    protected HttpResponse(int responseCode, String message) {
        this.message = message;
        this.responseCode = responseCode;
    }

    protected String getMessage() {
        if(message == null) {
            return "";
        }
        return message;
    }

    protected void setMessage(String message) {
        this.message = message;
    }

    protected int getResponseCode() {
        return responseCode;
    }

    protected void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    protected boolean isSuccessful() {
        return responseCode == HttpsURLConnection.HTTP_OK;
    }

    protected boolean isUnauthorized() {
        return responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED;
    }
}
