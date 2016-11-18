package com.mooduplabs.bugtracker;

import android.app.Application;

import com.mooduplabs.debuggit.DebuggIt;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DebuggIt.getInstance().initBitbucket("Jz9hKhxwAWgRNcS6m8", "dzyS7K5mnvcEWFtsS6veUM8RDJxRzwXQ", "bugreporter", "moodup");
        DebuggIt.getInstance().setRecordingEnabled(true);
//        DebuggIt.getInstance().initJira("januszowy.atlassian.net", "TP");
//        DebuggIt.getInstance().initGitHub("test", "arkus7");
    }
}
