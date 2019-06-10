package com.mooduplabs.bugtracker;

import android.app.Application;

import com.mooduplabs.debuggit.DebuggIt;

public class DebuggItDemoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DebuggIt.getInstance().setRecordingEnabled(true);
        DebuggIt.getInstance().setBaseUrl("https://debuggit-api-staging.herokuapp.com");
        DebuggIt.getInstance().initBitbucket("bugreporter", "moodup");
    }
}