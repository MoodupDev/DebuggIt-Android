package com.mooduplabs.bugtracker;

import android.app.Application;

import com.mooduplabs.debuggit.DebuggIt;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DebuggIt.getInstance().setRecordingEnabled(true);
        //DebuggIt.getInstance().initBitbucket("bugreporter", "moodup");
        //DebuggIt.getInstance().initJira("januszowy.atlassian.net", "TP");
        DebuggIt.getInstance().initGitHub("test", "MoodupDev");
    }
}