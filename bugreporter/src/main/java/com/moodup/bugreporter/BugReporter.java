package com.moodup.bugreporter;

import android.app.Activity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class BugReporter {

    private static BugReporter instance;

    private Activity activity;
    private ApiClient apiClient;
    private View reportButton;

    private String clientId;
    private String accessToken;

    public static BugReporter getInstance() {
        if (instance == null) {
            instance = new BugReporter();
        }

        return instance;
    }

    private BugReporter() {
    }

    public void init(String clientId) {
        this.apiClient = new ApiClient();
        this.clientId = clientId;
    }

    public void attach(Activity activity) {
        this.activity = activity;
        authenticate();
        addReportButton();
    }

    private void authenticate() {
        if (Utils.getString(activity, "access_token", "").isEmpty()) {
            final WebView webView = new WebView(activity);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

            final FrameLayout rootLayout = (FrameLayout) activity.findViewById(android.R.id.content);
            rootLayout.addView(webView);

            webView.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.contains("yolo.neze")) {
                        accessToken = Utils.getQueryMap(url).get("access_token");
                        Utils.putString(activity, "access_token", accessToken);
                        rootLayout.removeView(webView);
                        return true;
                    }

                    return false;
                }
            });

            webView.loadUrl("https://bitbucket.org/site/oauth2/authorize?client_id=" + clientId + "&response_type=token");
        }
    }

    private void addReportButton() {
        FrameLayout rootLayout = (FrameLayout) activity.findViewById(android.R.id.content);

        reportButton = LayoutInflater.from(activity).inflate(R.layout.report_button_layout, rootLayout, false);
        rootLayout.addView(reportButton);

        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = ((AppCompatActivity)activity).getSupportFragmentManager().beginTransaction();
                transaction.add(android.R.id.content, new ReporterFragment(), ReporterFragment.TAG);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    public void detach() {
        removeReportButton();
        activity = null;
    }

    private void removeReportButton() {
        ((ViewGroup) activity.findViewById(android.R.id.content)).removeView(reportButton);
    }

}
