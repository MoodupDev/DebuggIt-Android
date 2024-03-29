package com.mooduplabs.debuggit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.HashMap;

class GitHubApiService implements ApiService {
    private static final String TOKEN_FORMAT = "token %s";
    private static final String AUTH_NOTE_FORMAT = "debugg.it library at %s";

    private String accessToken;
    private String accountName;
    private String repoSlug;
    private String twoFactorAuthCode;

    protected GitHubApiService(String accountName, String repoSlug) {
        this.repoSlug = repoSlug;
        this.accountName = accountName;
    }

    @Override
    public void login(String email, String password, JsonResponseCallback callback) {
        try {
            HttpClient client = HttpClient.post(Constants.GitHub.AUTHORIZE_URL)
                    .withHeader(HttpClient.ACCEPT_HEADER, Constants.GitHub.JSON_FORMAT)
                    .authUser(email, password)
                    .withData(getAuthJsonObject());
            if (twoFactorAuthCode != null && !twoFactorAuthCode.isEmpty()) {
                client.withHeader("X-GitHub-OTP", twoFactorAuthCode);
            }
            client.send(callback);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exchangeAuthCodeForToken(String code, JsonResponseCallback callback) {
        try {
            HttpClient client = HttpClient.post(Constants.GitHub.ACCESS_TOKEN_URL)
                    .withHeader(HttpClient.ACCEPT_HEADER, Constants.GitHub.JSON_STANDARD_FORMAT)
                    .withData(getAuthHashMap(code));
            client.send(callback);
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addIssue(String title, String content, String priority, String kind, StringResponseCallback callback) {
        try {
            HttpClient.post(String.format(Constants.GitHub.ISSUES_URL, accountName, repoSlug))
                    .withHeader(HttpClient.ACCEPT_HEADER, Constants.GitHub.JSON_FORMAT)
                    .withHeader(HttpClient.AUTHORIZATION_HEADER, String.format(TOKEN_FORMAT, accessToken))
                    .withData(getIssueJsonObject(title, content, kind))
                    .send(callback);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void refreshToken(String refreshToken, JsonResponseCallback callback) {
        // tokens don't have to expire
    }

    private JSONObject getAuthJsonObject() {
        JSONObject auth = new JSONObject();
        JSONArray scopes = new JSONArray();
        scopes.put(Constants.GitHub.SCOPE_REPO);
        try {
            auth.put(Constants.Keys.SCOPES, scopes);
            auth.put(Constants.Keys.NOTE, String.format(AUTH_NOTE_FORMAT, Utils.getDateString(System.currentTimeMillis())));
            auth.put(Constants.Keys.NOTE_URL, Constants.DEBUGGIT_URL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return auth;
    }

    private HashMap<String, String> getAuthHashMap(String code) {
        HashMap<String, String> auth = new HashMap<>();

        auth.put(Constants.Keys.CLIENT_ID, Constants.GitHub.CLIENT_ID);
        auth.put(Constants.Keys.CLIENT_SECRET, Constants.GitHub.CLIENT_SECRET);
        auth.put(Constants.Keys.CODE, code);

        return auth;
    }

    private JSONObject getIssueJsonObject(String title, String content, String kind) {
        JSONObject issue = new JSONObject();
        try {
            issue.put(Constants.Keys.TITLE, title);
            issue.put(Constants.Keys.BODY, content);
            JSONArray labels = new JSONArray();
            labels.put(kind);
            issue.put(Constants.Keys.LABELS, labels);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return issue;
    }

    protected void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    protected void setTwoFactorAuthCode(String twoFactorAuthCode) {
        this.twoFactorAuthCode = twoFactorAuthCode;
    }
}
