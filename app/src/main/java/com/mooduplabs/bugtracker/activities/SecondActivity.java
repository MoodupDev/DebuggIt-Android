package com.mooduplabs.bugtracker.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.mooduplabs.bugtracker.R;
import com.mooduplabs.bugtracker.helpers.DebuggItWebViewClient;

public class SecondActivity extends BaseActivity {

    @Override
    protected int getLayout() {
        return R.layout.activity_secondary;
    }

    @Override
    protected void onActivityReady(@Nullable Bundle savedInstanceState) {
        configureWebView();
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