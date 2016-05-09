package com.moodup.bugreporter;

public interface BitBucket {
    String CALLBACK_URL = "http://callback.moodup.com";
    String OAUTH_URL = "https://bitbucket.org/site/oauth2/authorize?client_id=%1$s&response_type=token";
    String ISSUES_URL = "https://api.bitbucket.org/1.0/repositories/%1$s/%2$s/issues";
}
