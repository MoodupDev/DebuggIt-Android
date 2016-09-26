package com.mooduplabs.debuggit;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

public class JiraApiClient {

    private JiraConfig config;

    public JiraApiClient(JiraConfig config) {
        this.config = config;
    }

    protected void login(String username, String password, StringResponseCallback callback) {
        try {
            HttpClient.get(String.format(Constants.Jira.PROJECTS_URL, config.getHost())).authUser(username, password).send(callback);
        } catch(MalformedURLException e) {
            callback.onException(e);
        }
    }

    protected void addIssue(String title, String content, String priority, StringResponseCallback callback) {
        try {
            // TODO: 26.09.2016 refactor this
            JSONObject issue = new JSONObject();
            JSONObject fields = new JSONObject();
            JSONObject project = new JSONObject();
            project.put("key", config.getProjectKey());
            fields.put("project", project);
            fields.put("summary", title);
            fields.put("description", content);
            JSONObject issueType = new JSONObject();
            issueType.put("name", "Bug");
            fields.put("issuetype", issueType);
            issue.put("fields", fields);
            HttpClient.post(String.format(Constants.Jira.ISSUES_URL, config.getHost(), config.getProjectKey()))
                    .authUser(config.getUsername(), config.getPassword())
                    .withData(fields)
                    .send(callback);
        } catch(MalformedURLException e) {
            callback.onException(e);
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }



}
