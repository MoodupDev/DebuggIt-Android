package com.moodup.bugreporter;

public interface BitBucket {
    String REFERER_URL = "https://bitbucket.org";
    String CALLBACK_URL = "http://callback.moodup.com";
    String OAUTH_URL = "https://bitbucket.org/site/oauth2/authorize?client_id=%1$s&response_type=token";
    String ISSUES_URL = "https://api.bitbucket.org/1.0/repositories/%1$s/%2$s/issues";
    // PRIORITIES
    String PRIORITY_TRIVIAL = "trivial";
    String PRIORITY_MINOR = "minor";
    String PRIORITY_MAJOR = "major";
    String PRIORITY_CRITICAL = "critical";
    String PRIORITY_BLOCKER = "blocker";
    // ISSUE KINDS
    String KIND_BUG = "bug";
    String KIND_ENHANCEMENT = "enhancement";
    String KIND_PROPOSAL = "proposal";
    String KIND_TASK = "task";
}
