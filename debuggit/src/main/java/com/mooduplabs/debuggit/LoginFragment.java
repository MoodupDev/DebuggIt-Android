package com.mooduplabs.debuggit;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class LoginFragment extends DialogFragment {
    //region Consts

    public static final String TAG = LoginFragment.class.getSimpleName();
    public static final String BITBUCKET_LOGIN_PAGE = "https://bitbucket.org/site/oauth2/authorize?client_id=Jz9hKhxwAWgRNcS6m8&response_type=token";
    public static final String GITHUB_LOGIN_PAGE = "";
    public static final String JIRA_LOGIN_PAGE = "";
    public static final String ACCESS_TOKEN_STRING = "access_token=";

    //endregion

    //region Fields

    private WebView webView;
    private ProgressBar webViewProgressBar;

    //endregion

    //region Override Methods

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CustomDialog dialog = new CustomDialog(getActivity(), R.style.BrCustomDialog);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_br_login_webview, null);
        dialog.setContentView(view);

        initWebView(view);

        return dialog;
    }

    //endregion

    //region Methods

    public LoginFragment() {
        // Required empty public constructor
    }

    private void initWebView(View view) {
        webView = (WebView) view.findViewById(R.id.webview);
        webViewProgressBar = (ProgressBar) view.findViewById(R.id.webview_progress_bar);

        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                webViewProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                webViewProgressBar.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(ACCESS_TOKEN_STRING)) {
                    String token = url.substring(url.indexOf(ACCESS_TOKEN_STRING) + ACCESS_TOKEN_STRING.length(), url.indexOf("&")).replaceAll("%3D", "=");

                    switch (DebuggIt.getInstance().getConfigType()) {
                        case BITBUCKET:
                            handleBitBucketLoginResponse(token);
                            break;
                        case JIRA:
                            handleJiraLoginResponse();
                            break;
                        case GITHUB:
                            handleGitHubLoginResponse();
                            break;
                    }

                    webView.stopLoading();
                    return true;
                }

                return false;
            }
        });

        switch (DebuggIt.getInstance().getConfigType()) {
            case BITBUCKET:
                webView.loadUrl(BITBUCKET_LOGIN_PAGE);
                break;
            case JIRA:
                webView.loadUrl(JIRA_LOGIN_PAGE);
                break;
            case GITHUB:
                webView.loadUrl(GITHUB_LOGIN_PAGE);
                break;
        }
    }

    private void handleBitBucketLoginResponse(String token) {
        DebuggIt.getInstance().saveToken(token);
        ConfirmationDialog.newInstance(getString(R.string.br_login_successful), false).show(getChildFragmentManager(), ConfirmationDialog.TAG);
    }

    private void handleJiraLoginResponse() {
    }

    private void handleGitHubLoginResponse() {
    }

    protected static LoginFragment newInstance() {
        return new LoginFragment();
    }

    //endregion


}
