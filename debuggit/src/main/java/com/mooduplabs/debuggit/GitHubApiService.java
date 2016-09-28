package com.mooduplabs.debuggit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

public class GitHubApiService implements ApiService {
    //region Consts

    //endregion

    //region Fields

    private String accessToken;
    private String accountName;
    private String repoSlug;

    //endregion

    //region Override Methods

    @Override
    public void login(String email, String password, JsonResponseCallback callback) {
        try {
            HttpClient.post(Constants.GitHub.AUTHORIZE_URL)
                    .withHeader("Accept", Constants.GitHub.JSON_FORMAT)
                    .authUser(email, password)
                    .withData(getAuthJsonObject())
                    .send(callback);
        } catch(MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addIssue(String title, String content, String priority, String kind, StringResponseCallback callback) {
        try {
            HttpClient.post(String.format(Constants.GitHub.ISSUES_URL, accountName, repoSlug))
                    .withHeader("Accept", Constants.GitHub.JSON_FORMAT)
                    .withHeader("Authorization", String.format("token %s", accessToken))
                    .withData(getIssueJsonObject(title, content, kind))
                    .send(callback);
        } catch(MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void refreshToken(String refreshToken, JsonResponseCallback callback) {
        // tokens don't have to expire
    }


    //endregion

    //region Events

    //endregion

    //region Methods


    public GitHubApiService(String accountName, String repoSlug) {
        this.accountName = accountName;
        this.repoSlug = repoSlug;
    }

    private JSONObject getAuthJsonObject() {
        JSONObject auth = new JSONObject();
        JSONArray scopes = new JSONArray();
        scopes.put("repo");
        try {
            auth.put("scopes", scopes);
            auth.put("note", "Debugg.it library" + System.currentTimeMillis());
            auth.put("note_url", "http://debugg.it");
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return auth;
    }


    private JSONObject getIssueJsonObject(String title, String content, String kind) {
        JSONObject issue = new JSONObject();
        try {
            issue.put("title", title);
            issue.put("body", content);
            JSONArray labels = new JSONArray();
            labels.put(kind);
            issue.put("labels", labels);
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return issue;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    //endregion


}
