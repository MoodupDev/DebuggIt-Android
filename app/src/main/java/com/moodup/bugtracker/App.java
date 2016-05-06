package com.moodup.bugtracker;

import android.app.Application;

import com.moodup.bugreporter.BugReporter;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BugReporter.getInstance().init("C9PnuH4fPyvUDjFwMz", "bugreporter", "moodup");
    }
}
