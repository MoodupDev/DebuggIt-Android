package com.moodup.bugreporter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.UrlQuerySanitizer;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BugReporter {

    private static BugReporter instance;

    private Context appContext;
    private Activity activity;
    private ApiClient apiClient;

    private String clientId;
    private String accessToken;

    public static BugReporter getInstance() {
        if (instance == null) {
            instance = new BugReporter();
        }

        return instance;
    }

    public void init(String clientId, Context appContext) {
        this.appContext = appContext;
        this.apiClient = new ApiClient();

        if (getString("access_token", "").isEmpty()) {
            WebView webView = new WebView(appContext);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    if (url.contains("yolo.neze")) {
                        UrlQuerySanitizer.ValueSanitizer sanitizer = UrlQuerySanitizer.getAllButNulLegal();
                        accessToken = sanitizer.sanitize("access_token");

                        putString("access_token", accessToken);
                    }
                }
            });

            webView.loadUrl("https://bitbucket.org/site/oauth2/authorize?client_id=" + clientId + "&response_type=token");
        }
    }

    public void attach(Activity activity) {
        this.activity = activity;
    }

    public void deattach() {
        activity = null;
    }

    private void putString(String key, String value) {
        SharedPreferences prefs = appContext.getSharedPreferences(appContext.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(key, value);
        editor.commit();
    }

    private String getString(String key, String defValue) {
        return appContext.getSharedPreferences(appContext.getPackageName(), Context.MODE_PRIVATE).getString(key, defValue);
    }

}
