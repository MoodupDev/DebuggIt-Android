package com.mooduplabs.debuggit;

public class JiraConfig {
    //region Consts

    //endregion

    //region Fields

    private String host;
    private String projectKey;
    private String username;
    private String password;

    //endregion

    //region Override Methods

    //endregion

    //region Events

    //endregion

    //region Methods

    public JiraConfig(String host, String projectKey) {
        this.host = host;
        this.projectKey = projectKey;
    }

    public String getHost() {
        return host;
    }

    public String getProjectKey() {
        return projectKey;
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

    //endregion


}
