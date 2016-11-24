package com.mooduplabs.debuggit;

interface Constants {

    String PRIORITY_LOW = "Low";
    String PRIORITY_MEDIUM = "Medium";
    String PRIORITY_HIGH = "High";

    // ISSUE KINDS
    String KIND_BUG = "bug";
    String KIND_ENHANCEMENT = "enhancement";

    String DEBUGGIT_URL = "http://debugg.it";

    interface BitBucket {
        // CREDENTIALS
        String CLIENT_ID = "Jz9hKhxwAWgRNcS6m8";
        String CLIENT_SECRET = "dzyS7K5mnvcEWFtsS6veUM8RDJxRzwXQ";

        // URLS
        String LOGIN_PAGE = "https://bitbucket.org/site/oauth2/authorize?client_id=%s&response_type=token";
        String AUTHORIZE_URL = "https://bitbucket.org/site/oauth2/access_token";
        String ISSUES_URL = "https://api.bitbucket.org/1.0/repositories/%1$s/%2$s/issues";

        // PRIORITIES
        String PRIORITY_MINOR = "minor";
        String PRIORITY_MAJOR = "major";
        String PRIORITY_CRITICAL = "critical";

        // AUTH
        String GRANT_TYPE_PASSWORD = "password";
        String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

        String ACCESS_TOKEN = "access_token";
        String REFRESH_TOKEN = "refresh_token";
    }

    interface Jira {
        String LOGIN_PAGE = "";
        String ISSUES_URL = "https://%s/rest/api/2/issue";
        String CONFIGURATION_URL = "https://%s/rest/api/2/configuration";

        String KIND_BUG = "Bug";
        String KIND_TASK = "Task";

        String EMAIL = "jira_email";
        String PASSWORD = "jira_password";
    }

    interface GitHub {
        // CREDENTIALS
        String CLIENT_ID = "8aac9632491f7d954664";
        String CLIENT_SECRET = "1b7bdf305e08971b3c95c1cfc06fc05eebd59707";

        // URLS
        String LOGIN_PAGE = "https://github.com/login/oauth/authorize?client_id=%s&scope=repo";
        String AUTHORIZE_URL = "https://api.github.com/authorizations";
        String ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
        String ISSUES_URL = "https://api.github.com/repos/%1$s/%2$s/issues";

        String JSON_FORMAT = "application/vnd.github.v3+json";
        String JSON_STANDARD_FORMAT = "application/json";

        String ACCESS_TOKEN = "access_token";
        String GITHUB_ACCESS_TOKEN = "github_access_token";
        String TWO_FACTOR_AUTH_CODE = "github_2fa_code";
        String SCOPE_REPO = "repo";
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
        String SYSTEM_VERSION = "system_version";
        String SYSTEM = "system";
        String ANDROID = "android";
        String DEVICE = "device";
        String VALUE = "value";
        String DATA = "data";
        String PROJECT_KEY = "key";
        String PROJECT = "project";
        String SUMMARY = "summary";
        String DESCRIPTION = "description";
        String FIELD_NAME = "name";
        String ISSUE_TYPE = "issuetype";
        String FIELDS = "fields";
        String TOKEN = "token";
        String SCOPES = "scopes";
        String NOTE = "note";
        String NOTE_URL = "note_url";
        String LABELS = "labels";
        String BODY = "body";
        String HAS_WELCOME_SCREEN = "has_welcome_screen";
        String CLIENT_ID = "client_id";
        String CLIENT_SECRET = "client_secret";
        String CODE = "code";
    }
}
