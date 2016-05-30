package com.moodup.bugreporter;

public class HttpResponse {
    String message;
    int responseCode;

    protected HttpResponse() {
    }

    protected HttpResponse(int responseCode, String message) {
        this.message = message;
        this.responseCode = responseCode;
    }

    protected String getMessage() {
        if (message == null)
            return "";
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

}
