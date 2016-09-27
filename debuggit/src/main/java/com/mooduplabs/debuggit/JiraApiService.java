package com.mooduplabs.debuggit;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

class JiraApiService implements ApiService {

    private String host;
    private String projectKey;
    private String username;
    private String password;

    public JiraApiService(String host, String projectKey) {
        this.host = host;
        this.projectKey = projectKey;
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
            HttpClient.get(String.format(Constants.Jira.CONFIGURATION_URL, host)).authUser(email, password).send(callback);
        } catch(MalformedURLException e) {
            callback.onException(e);
        }
    }

    @Override
    public void addIssue(String title, String content, String priority, String kind, StringResponseCallback callback) {
        try {
            // TODO: 26.09.2016 refactor this
            JSONObject issue = new JSONObject();
            JSONObject fields = new JSONObject();
            JSONObject project = new JSONObject();
            project.put("key", projectKey);
            fields.put("project", project);
            fields.put("summary", title);
            fields.put("description", content);
            JSONObject issueType = new JSONObject();
            String issueTypeName = kind.equalsIgnoreCase(Constants.BitBucket.KIND_BUG) ? "Bug" : "Task";
            issueType.put("name", issueTypeName);
            fields.put("issuetype", issueType);
            issue.put("fields", fields);
            HttpClient.post(String.format(Constants.Jira.ISSUES_URL, host))
                    .authUser(username, password)
                    .withData(issue)
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
}
