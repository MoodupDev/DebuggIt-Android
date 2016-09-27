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
            HttpClient.post(String.format(Constants.Jira.ISSUES_URL, host))
                    .authUser(username, password)
                    .withData(getIssueObject(title, content, priority, kind))
                    .send(callback);
        } catch(MalformedURLException e) {
            callback.onException(e);
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject getIssueObject(String title, String content, String priority, String kind) throws JSONException {
        JSONObject issue = new JSONObject();
        JSONObject fields = new JSONObject();
        JSONObject project = new JSONObject();
        JSONObject issueType = new JSONObject();
        JSONObject priorityObject = new JSONObject();

        project.put(Constants.Keys.PROJECT_KEY, projectKey);
        issueType.put(Constants.Keys.FIELD_NAME, getJiraKind(kind));
        priorityObject.put(Constants.Keys.FIELD_NAME, getJiraPriority(priority));

        fields.put(Constants.Keys.PROJECT, project);
        fields.put(Constants.Keys.SUMMARY, title);
        fields.put(Constants.Keys.DESCRIPTION, content);
        fields.put(Constants.Keys.ISSUE_TYPE, issueType);
        fields.put(Constants.Keys.PRIORITY, priorityObject);

        issue.put(Constants.Keys.FIELDS, fields);
        return issue;
    }

    @Override
    public void refreshToken(String refreshToken, JsonResponseCallback callback) {
        // do nothing
    }

    private String getJiraKind(String kind) {
        return kind.equalsIgnoreCase(Constants.BitBucket.KIND_BUG) ? Constants.Jira.KIND_BUG : Constants.Jira.KIND_TASK;
    }

    private String getJiraPriority(String priority) {
        switch(priority) {
            case Constants.BitBucket.PRIORITY_MINOR:
                return Constants.Jira.PRIORITY_LOW;
            case Constants.BitBucket.PRIORITY_MAJOR:
                return Constants.Jira.PRIORITY_MEDIUM;
            case Constants.BitBucket.PRIORITY_CRITICAL:
                return Constants.Jira.PRIORITY_HIGH;
        }
        return Constants.Jira.PRIORITY_MEDIUM;
    }
}
