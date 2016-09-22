package com.mooduplabs.bugtracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.mooduplabs.debuggit.DebuggIt;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Toast.makeText(this, "Toast", Toast.LENGTH_LONG).show();
        Snackbar.make(findViewById(android.R.id.content), "Snackbar", Snackbar.LENGTH_LONG).show();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dialog");
        builder.setMessage("Shake your phone to take screenshot");
        builder.setPositiveButton("Crash me", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                throw new RuntimeException("crash button clicked");
            }
        });
        builder.show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        DebuggIt.getInstance().attach(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DebuggIt.getInstance().getScreenshotPermission(requestCode, resultCode, data);
    }
}