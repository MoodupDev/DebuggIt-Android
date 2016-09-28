package com.mooduplabs.debuggit;

interface Constants {

    String PRIORITY_LOW = "Low";
    String PRIORITY_MEDIUM = "Medium";
    String PRIORITY_HIGH = "High";

    // ISSUE KINDS
    String KIND_BUG = "bug";
    String KIND_ENHANCEMENT = "enhancement";

    interface BitBucket {
        // URLS
        String AUTHORIZE_URL = "https://bitbucket.org/site/oauth2/access_token";
        String ISSUES_URL = "https://api.bitbucket.org/1.0/repositories/%1$s/%2$s/issues";

        // PRIORITIES
        String PRIORITY_MINOR = "minor";
        String PRIORITY_MAJOR = "major";
        String PRIORITY_CRITICAL = "critical";

        // AUTH
        String GRANT_TYPE_PASSWORD = "password";
        String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    }

    interface Jira {
        String ISSUES_URL = "https://%s/rest/api/2/issue";
        String CONFIGURATION_URL = "https://%s/rest/api/2/configuration";
        String KIND_BUG = "Bug";
        String KIND_TASK = "Task";
    }

    interface Keys {
        String TITLE = "title";
        String CONTENT = "content";
        String PRIORITY = "priority";
        String KIND = "kind";
        String GRANT_TYPE = "grant_type";
        String USERNAME = "username";
        String EVENT_TYPE = "event_type";
        String APP_ID = "app_id";
        String ANDROID_SDK = "android_sdk";
        String DEVICE = "device";
        String VALUE = "value";
        String DATA = "data";
        String JIRA_EMAIL = "jira_email";
        String JIRA_PASSWORD = "jira_password";
        String PROJECT_KEY = "key";
        String PROJECT = "project";
        String SUMMARY = "summary";
        String DESCRIPTION = "description";
        String FIELD_NAME = "name";
        String ISSUE_TYPE = "issuetype";
        String FIELDS = "fields";
    }
}
