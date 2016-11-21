package com.mooduplabs.bugtracker;

import android.app.Application;

import com.mooduplabs.debuggit.DebuggIt;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //DebuggIt.getInstance().initBitbucket("Jz9hKhxwAWgRNcS6m8", "dzyS7K5mnvcEWFtsS6veUM8RDJxRzwXQ", "bugreporter", "moodup");
        DebuggIt.getInstance().setRecordingEnabled(true);
        //DebuggIt.getInstance().initJira("januszowy.atlassian.net", "TP");
        DebuggIt.getInstance().initGitHub("8aac9632491f7d954664", "1b7bdf305e08971b3c95c1cfc06fc05eebd59707", "test", "MoodupDev");
    }
}