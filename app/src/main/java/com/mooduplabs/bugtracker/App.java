package com.mooduplabs.bugtracker;

import android.app.Application;

import com.mooduplabs.debuggit.BugReporter;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BugReporter.getInstance().init("Jz9hKhxwAWgRNcS6m8", "dzyS7K5mnvcEWFtsS6veUM8RDJxRzwXQ", "bugreporter", "moodup");
    }
}
