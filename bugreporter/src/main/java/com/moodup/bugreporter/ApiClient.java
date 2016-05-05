package com.moodup.bugreporter;

import java.io.InputStream;

public class ApiClient {

    private ApiService apiService;

    public ApiClient() {

    }

    public void getAccessToken(String clientId) {

    }

    public void refreshToken(String refreshToken) {

    }

    public void addIssue() {

    }

    /*
     * throws null when input stream is null
     */
    public String uploadPic(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream can't be null!");
        }

        return "";
    }
}
