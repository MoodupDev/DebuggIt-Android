package com.mooduplabs.debuggit;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.HashMap;

class BitBucketApiService implements ApiService {
    private String repoSlug;
    private String accountName;
    private String accessToken;

    public BitBucketApiService(String repoSlug, String accountName) {
        this.repoSlug = repoSlug;
        this.accountName = accountName;
    }

    @Override
    public void addIssue(String title, String content, String priority, String kind, StringResponseCallback callback) {
        try {
            JSONObject data = new JSONObject();
            data.put(Constants.Keys.TITLE, title);

            JSONObject contentData = new JSONObject();
            contentData.put(Constants.Keys.RAW, content);
            contentData.put(Constants.Keys.MARKUP, Constants.Keys.MARKDOWN);

            data.put(Constants.Keys.CONTENT, contentData);
            data.put(Constants.Keys.PRIORITY, priority);
            data.put(Constants.Keys.KIND, kind);

            HttpClient.post(String.format(Constants.BitBucket.ISSUES_URL, accountName, repoSlug)).withData(data).authUser(accessToken).send(callback);
        } catch (JSONException | MalformedURLException e) {
            callback.onException(e);
        }
    }

    @Override
    public void refreshToken(String refreshToken, JsonResponseCallback callback) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.Keys.GRANT_TYPE, Constants.BitBucket.GRANT_TYPE_REFRESH_TOKEN);
        map.put(Constants.BitBucket.GRANT_TYPE_REFRESH_TOKEN, refreshToken);

        try {
            HttpClient.post(Constants.BitBucket.AUTHORIZE_URL).withData(map).authUser(Constants.BitBucket.CLIENT_ID, Constants.BitBucket.CLIENT_SECRET).send(callback);
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            callback.onException(e);
        }
    }

    @Override
    public void login(String email, String password, JsonResponseCallback callback) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.Keys.GRANT_TYPE, Constants.BitBucket.GRANT_TYPE_PASSWORD);
        map.put(Constants.Keys.USERNAME, email);
        map.put(Constants.BitBucket.GRANT_TYPE_PASSWORD, password);

        try {
            HttpClient.post(Constants.BitBucket.AUTHORIZE_URL).withData(map).authUser(Constants.BitBucket.CLIENT_ID, Constants.BitBucket.CLIENT_SECRET).send(callback);
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            callback.onException(e);
        }
    }

    @Override
    public void exchangeAuthCodeForToken(String code, JsonResponseCallback callback) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.Keys.GRANT_TYPE, Constants.BitBucket.GRANT_TYPE_AUTHORIZATION_CODE);
        map.put(Constants.Keys.CODE, code);

        try {
            HttpClient.post(Constants.BitBucket.AUTHORIZE_URL)
                    .withData(map)
                    .authUser(Constants.BitBucket.CLIENT_ID, Constants.BitBucket.CLIENT_SECRET)
                    .send(callback);
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            callback.onException(e);
        }
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
