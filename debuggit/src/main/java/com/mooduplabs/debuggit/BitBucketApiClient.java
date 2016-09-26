package com.mooduplabs.debuggit;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.HashMap;

public class BitBucketApiClient {
    //region Consts

    //endregion

    //region Fields

    private BitBucketConfig config;

    //endregion

    //region Override Methods

    //endregion

    //region Events

    //endregion

    //region Methods

    public BitBucketApiClient(BitBucketConfig config) {
        this.config = config;
    }

    protected void addIssue(String title, String content, String priority, String kind, StringResponseCallback callback) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.Keys.TITLE, title);
        map.put(Constants.Keys.CONTENT, content);
        map.put(Constants.Keys.PRIORITY, priority);
        map.put(Constants.Keys.KIND, kind);

        try {
            HttpClient.post(String.format(Constants.BitBucket.ISSUES_URL, config.getAccountName(), config.getRepoSlug())).withData(map).authUser(config.getAccessToken()).send(callback);
        } catch(UnsupportedEncodingException | MalformedURLException e) {
            callback.onException(e);
        }
    }

    protected void refreshToken(String refreshToken, JsonResponseCallback callback) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.Keys.GRANT_TYPE, Constants.BitBucket.GRANT_TYPE_REFRESH_TOKEN);
        map.put(Constants.BitBucket.GRANT_TYPE_REFRESH_TOKEN, refreshToken);

        try {
            HttpClient.post(Constants.BitBucket.AUTHORIZE_URL).withData(map).authUser(config.getClientId(), config.getClientSecret()).send(callback);
        } catch(UnsupportedEncodingException | MalformedURLException e) {
            callback.onException(e);
        }
    }

    protected void login(String clientId, String clientSecret, String email, String password, JsonResponseCallback callback) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.Keys.GRANT_TYPE, Constants.BitBucket.GRANT_TYPE_PASSWORD);
        map.put(Constants.Keys.USERNAME, email);
        map.put(Constants.BitBucket.GRANT_TYPE_PASSWORD, password);

        try {
            HttpClient.post(Constants.BitBucket.AUTHORIZE_URL).withData(map).authUser(clientId, clientSecret).send(callback);
        } catch(UnsupportedEncodingException | MalformedURLException e) {
            callback.onException(e);
        }
    }

    //endregion


}
