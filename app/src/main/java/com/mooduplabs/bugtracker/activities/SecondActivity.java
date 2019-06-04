package com.mooduplabs.bugtracker.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.mooduplabs.bugtracker.R;
import com.mooduplabs.bugtracker.helpers.DebuggItWebViewClient;
import com.mooduplabs.debuggit.DebuggIt;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary);

        configureWebView();
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

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        WebView webView = findViewById(R.id.sc_web);
        webView.loadUrl("https://debugg.it/developers.html");
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new DebuggItWebViewClient(getApplicationContext()));
    }
}