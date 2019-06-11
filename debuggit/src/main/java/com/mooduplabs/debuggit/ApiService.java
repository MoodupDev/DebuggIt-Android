package com.mooduplabs.debuggit;

interface ApiService {
    void login(String email, String password, JsonResponseCallback callback);

    void exchangeAuthCodeForToken(String code, JsonResponseCallback callback);

    void addIssue(String title, String content, String priority, String kind, StringResponseCallback callback);

    void refreshToken(String refreshToken, JsonResponseCallback callback);
}
