package com.moodup.bugreporter;

public interface BitBucket {

    // URLS
    String AUTHORIZE_URL = "https://bitbucket.org/site/oauth2/access_token";
    String ISSUES_URL = "https://api.bitbucket.org/1.0/repositories/%1$s/%2$s/issues";

    // PRIORITIES
    String PRIORITY_MINOR = "minor";
    String PRIORITY_MAJOR = "major";
    String PRIORITY_CRITICAL = "critical";

    // ISSUE KINDS
    String KIND_BUG = "bug";
    String KIND_ENHANCEMENT = "enhancement";

    // AUTH
    String GRANT_TYPE_PASSWORD = "password";
    String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
}
