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

import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

public class LoginFragment extends DialogFragment {
    //region Consts

    public static final String TAG = LoginFragment.class.getSimpleName();

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
                                     public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                                         switch (DebuggIt.getInstance().getConfigType()) {
                                             case BITBUCKET:
                                                 if (url.contains(Constants.BitBucket.ACCESS_TOKEN)) {
                                                     String token = url.substring(url.indexOf(Constants.BitBucket.ACCESS_TOKEN) + Constants.BitBucket.ACCESS_TOKEN.length() + 1, url.indexOf("&"))
                                                             .replaceAll("%3D", "=");

                                                     handleBitBucketLoginResponse(token);

                                                     webView.stopLoading();
                                                     return true;
                                                 }
                                                 break;
                                             case JIRA:
                                                 handleJiraLoginResponse();
                                                 break;
                                             case GITHUB:
                                                 if (url.contains(Constants.Keys.CODE)) {
                                                     String code = url.substring(url.indexOf(Constants.Keys.CODE) + Constants.Keys.CODE.length() + 1);

                                                     DebuggIt.getInstance().getApiService().loginWithOAuth(
                                                             code,
                                                             new JsonResponseCallback() {
                                                                 @Override
                                                                 public void onSuccess(JSONObject response) {
                                                                     try {
                                                                         handleGitHubLoginResponse(response.getString(Constants.GitHub.ACCESS_TOKEN));
                                                                     } catch (JSONException e) {
                                                                         e.printStackTrace();
                                                                     }
                                                                 }

                                                                 @Override
                                                                 public void onFailure(int responseCode, String errorMessage) {
                                                                     if (responseCode == HttpsURLConnection.HTTP_BAD_REQUEST || responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                                                                         ConfirmationDialog.newInstance(Utils.getBitbucketErrorMessage(errorMessage, getString(R.string.br_login_error_wrong_credentials)), true)
                                                                                 .show(getChildFragmentManager(), ConfirmationDialog.TAG);
                                                                     } else {
                                                                         ConfirmationDialog.newInstance(getContext().getString(R.string.br_login_error), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
                                                                     }
                                                                 }

                                                                 @Override
                                                                 public void onException(Exception ex) {
                                                                     ConfirmationDialog.newInstance(getContext().getString(R.string.br_login_error), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
                                                                 }
                                                             });

                                                     webView.stopLoading();
                                                     return true;
                                                 }
                                                 break;
                                         }
                                         return false;
                                     }
                                 }

        );

        switch (DebuggIt.getInstance().getConfigType()) {
            case BITBUCKET:
                webView.loadUrl(String.format(Constants.BitBucket.LOGIN_PAGE, Constants.BitBucket.CLIENT_ID));
                break;
            case JIRA:
                webView.loadUrl(Constants.Jira.LOGIN_PAGE);
                break;
            case GITHUB:
                webView.loadUrl(String.format(Constants.GitHub.LOGIN_PAGE, Constants.GitHub.CLIENT_ID));
                break;
        }

    }

    private void handleBitBucketLoginResponse(String token) {
        DebuggIt.getInstance().saveToken(token);
        ConfirmationDialog.newInstance(getString(R.string.br_login_successful), false).show(getChildFragmentManager(), ConfirmationDialog.TAG);
    }

    private void handleJiraLoginResponse() {
    }

    private void handleGitHubLoginResponse(String token) {
        DebuggIt.getInstance().saveToken(token);
        ConfirmationDialog.newInstance(getString(R.string.br_login_successful), false).show(getChildFragmentManager(), ConfirmationDialog.TAG);
    }

    protected static LoginFragment newInstance() {
        return new LoginFragment();
    }

    //endregion


}
