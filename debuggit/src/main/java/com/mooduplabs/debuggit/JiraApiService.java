package com.mooduplabs.debuggit;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

class JiraApiService implements ApiService {

    private String host;
    private String projectKey;
    private String username;
    private String password;
    private boolean usesHttps;

    public JiraApiService(String host, String projectKey, boolean usesHttps) {
        this.host = host;
        this.projectKey = projectKey;
        this.usesHttps = usesHttps;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void login(String email, String password, JsonResponseCallback callback) {
        try {
            HttpClient.get(getUrlWithCorrectProtocol(String.format(Constants.Jira.CONFIGURATION_URL, host))).authUser(email, password).send(callback);
        } catch(MalformedURLException e) {
            callback.onException(e);
        }
    }

    @Override
    public void loginWithOAuth(String code, JsonResponseCallback callback) {

    }

    @Override
    public void addIssue(String title, String content, String priority, String kind, StringResponseCallback callback) {
        try {
            HttpClient.post(getUrlWithCorrectProtocol(String.format(Constants.Jira.ISSUES_URL, host)))
                    .authUser(username, password)
                    .withData(getIssueObject(title, content, priority, kind))
                    .send(callback);
        } catch(MalformedURLException e) {
            callback.onException(e);
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void refreshToken(String refreshToken, JsonResponseCallback callback) {
        // do nothing
    }

    private String getUrlWithCorrectProtocol(String url) {
        if(!usesHttps) {
            url = url.replaceFirst("s", "");
        }
        return url;
    }

    private JSONObject getIssueObject(String title, String content, String priority, String kind) throws JSONException {
        JSONObject issue = new JSONObject();
        JSONObject fields = new JSONObject();
        JSONObject project = new JSONObject();
        JSONObject issueType = new JSONObject();
        JSONObject priorityObject = new JSONObject();

        project.put(Constants.Keys.PROJECT_KEY, projectKey);
        issueType.put(Constants.Keys.FIELD_NAME, getJiraKind(kind));
        priorityObject.put(Constants.Keys.FIELD_NAME, priority);

        fields.put(Constants.Keys.PROJECT, project);
        fields.put(Constants.Keys.SUMMARY, title);
        fields.put(Constants.Keys.DESCRIPTION, content);
        fields.put(Constants.Keys.ISSUE_TYPE, issueType);
        fields.put(Constants.Keys.PRIORITY, priorityObject);

        issue.put(Constants.Keys.FIELDS, fields);
        return issue;
    }

    private String getJiraKind(String kind) {
        return kind.equalsIgnoreCase(Constants.KIND_BUG) ? Constants.Jira.KIND_BUG : Constants.Jira.KIND_TASK;
    }
}
