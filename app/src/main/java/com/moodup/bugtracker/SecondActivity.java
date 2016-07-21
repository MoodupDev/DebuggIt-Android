package com.moodup.bugtracker;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.moodup.bugreporter.BugReporter;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Toast.makeText(this, "Toast", Toast.LENGTH_LONG).show();
        Snackbar.make(findViewById(android.R.id.content), "Snackbar", Snackbar.LENGTH_LONG).show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        BugReporter.getInstance().attach(this);
    }
}