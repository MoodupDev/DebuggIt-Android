package com.mooduplabs.debuggit;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.HashMap;

public class BitBucketApiService implements ApiService {

    //region Fields

    private String clientId;
    private String clientSecret;
    private String repoSlug;
    private String accountName;
    private String accessToken;

    //endregion

    //region Override Methods

    @Override
    public void addIssue(String title, String content, String priority, String kind, StringResponseCallback callback) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.Keys.TITLE, title);
        map.put(Constants.Keys.CONTENT, content);
        map.put(Constants.Keys.PRIORITY, priority);
        map.put(Constants.Keys.KIND, kind);

        try {
            HttpClient.post(String.format(Constants.BitBucket.ISSUES_URL, accountName, repoSlug)).withData(map).authUser(accessToken).send(callback);
        } catch(UnsupportedEncodingException | MalformedURLException e) {
            callback.onException(e);
        }
    }

    @Override
    public void refreshToken(String refreshToken, JsonResponseCallback callback) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.Keys.GRANT_TYPE, Constants.BitBucket.GRANT_TYPE_REFRESH_TOKEN);
        map.put(Constants.BitBucket.GRANT_TYPE_REFRESH_TOKEN, refreshToken);

        try {
            HttpClient.post(Constants.BitBucket.AUTHORIZE_URL).withData(map).authUser(clientId, clientSecret).send(callback);
        } catch(UnsupportedEncodingException | MalformedURLException e) {
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
            HttpClient.post(Constants.BitBucket.AUTHORIZE_URL).withData(map).authUser(clientId, clientSecret).send(callback);
        } catch(UnsupportedEncodingException | MalformedURLException e) {
            callback.onException(e);
        }
    }

    @Override
    public void loginWithOAuth(String code, JsonResponseCallback callback) {

    }

    //endregion

    //region Methods

    public BitBucketApiService(String clientId, String clientSecret, String repoSlug, String accountName) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.repoSlug = repoSlug;
        this.accountName = accountName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    //endregion


}
