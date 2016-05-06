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

import butterknife.ButterKnife;

public class BugReporter {

    private static BugReporter instance;

    private Activity activity;
    private View reportButton;

    private String clientId;
    private String repoSlug;
    private String accountName;
    private String accessToken;

    public static BugReporter getInstance() {
        if (instance == null) {
            instance = new BugReporter();
        }

        return instance;
    }

    private BugReporter() {
    }

    public void init(String clientId, String repoSlug, String accountName) {
        this.clientId = clientId;
        this.repoSlug = repoSlug;
        this.accountName = accountName;
    }

    public void attach(Activity activity) {
        this.activity = activity;
        authenticate(false);
        addReportButton();
    }

    protected void authenticate(boolean refresh) {
        if (refresh) {
            Utils.putString(activity, "accessToken", "");
        }

        if (Utils.getString(activity, "accessToken", "").isEmpty()) {
            final WebView webView = new WebView(activity);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

            final FrameLayout rootLayout = ButterKnife.findById(activity, android.R.id.content);
            rootLayout.addView(webView);

            webView.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.contains(BitBucket.CALLBACK_URL)) {
                        accessToken = Utils.getQueryMap(url).get("access_token");
                        Utils.putString(activity, "accessToken", accessToken);
                        rootLayout.removeView(webView);
                        return true;
                    }

                    return false;
                }
            });

            webView.loadUrl(String.format(BitBucket.OAUTH_URL, clientId));
        }
    }

    private void addReportButton() {
        FrameLayout rootLayout = ButterKnife.findById(activity, android.R.id.content);

        reportButton = LayoutInflater.from(activity).inflate(R.layout.report_button_layout, rootLayout, false);
        rootLayout.addView(reportButton);

        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDrawFragment();
            }
        });
    }

    public void detach() {
        removeReportButton();
        activity = null;
    }

    private void removeReportButton() {
        ((ViewGroup) ButterKnife.findById(activity, android.R.id.content)).removeView(reportButton);
    }

    private void showDrawFragment() {
        FragmentTransaction transaction = ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction();
        transaction.add(android.R.id.content, new DrawFragment(), DrawFragment.TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    protected String getRepoSlug() {
        return repoSlug;
    }

    protected String getAccountName() {
        return accountName;
    }
}
