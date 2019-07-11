package com.mooduplabs.bugtracker.helpers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mooduplabs.bugtracker.R;

public class DebuggItWebViewClient extends WebViewClient {
    private Context context;

    public DebuggItWebViewClient(Context context) {
        this.context = context;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        String host = Uri.parse(url).getHost();

        if (host != null && host.equals(context.getString(R.string.debugg_it_website_host_url))) {
            // This is my website, so do not override; let my WebView load the page
            return false;
        }

        // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
        return true;
    }
}
