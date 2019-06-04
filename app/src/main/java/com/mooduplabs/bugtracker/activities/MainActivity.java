package com.mooduplabs.bugtracker.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.mooduplabs.bugtracker.R;
import com.mooduplabs.bugtracker.helpers.DebuggItWebViewClient;

public class MainActivity extends BaseActivity {

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void onActivityReady(@Nullable Bundle savedInstanceState) {
        configureWebView();

        new AlertDialog.Builder(this)
                .setTitle("Dialog")
                .setMessage("Shake your phone to take screenshot")
                .setPositiveButton(getString(android.R.string.ok), null)
                .show();

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runActivity(SecondActivity.class);
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        WebView webView = findViewById(R.id.mc_web);
        webView.loadUrl("https://debugg.it/");
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new DebuggItWebViewClient(getApplicationContext()));
    }
}
