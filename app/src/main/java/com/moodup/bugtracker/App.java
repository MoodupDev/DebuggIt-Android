package com.moodup.bugtracker;

import android.app.Application;

import com.moodup.bugreporter.BugReporter;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BugReporter.getInstance().init("Jz9hKhxwAWgRNcS6m8", "dzyS7K5mnvcEWFtsS6veUM8RDJxRzwXQ", "bugreporter", "moodup");
    }
}
