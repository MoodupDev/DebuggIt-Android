package com.mooduplabs.debuggit;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.DialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

public class WebLoginFragment extends DialogFragment {


    public static final String TAG = WebLoginFragment.class.getSimpleName();


    private WebView webView;
    private ProgressBar webViewProgressBar;
    private boolean keepProgressBarVisible = false;


    public WebLoginFragment() {
        // Required empty public constructor
    }


    protected static WebLoginFragment newInstance() {
        return new WebLoginFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CustomDialog dialog = new CustomDialog(getActivity(), R.style.BrCustomDialog);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_br_login_webview, null);
        dialog.setContentView(view);

        initWebViewProgressBar(view);
        initWebView(view);

        return dialog;
    }

    private void initWebViewProgressBar(View view) {
        webViewProgressBar = view.findViewById(R.id.webview_progress_bar);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Drawable wrapDrawable = DrawableCompat.wrap(webViewProgressBar.getIndeterminateDrawable());
            DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(getContext(), R.color.br_app_orange));
            webViewProgressBar.setIndeterminateDrawable(DrawableCompat.unwrap(wrapDrawable));
        } else {
            webViewProgressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.br_app_orange), PorterDuff.Mode.SRC_ATOP);
        }
    }

    private void initWebView(View view) {
        webView = view.findViewById(R.id.webview);

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
                                         if (keepProgressBarVisible) {
                                             keepProgressBarVisible = false;
                                         } else {
                                             webViewProgressBar.setVisibility(View.GONE);
                                         }
                                         super.onPageFinished(view, url);
                                     }

                                     @Override
                                     public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                                         webViewProgressBar.setVisibility(View.GONE);
                                         ConfirmationDialog.newInstance(getContext().getString(R.string.br_webview_page_error), true).show(getFragmentManager(), ConfirmationDialog.TAG);
                                         WebLoginFragment.this.dismiss();
                                     }

                                     @Override
                                     public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                                         if (url.contains(Constants.Keys.CALLBACK) && url.contains(Constants.Keys.CODE + "=")) {
                                             String code;

                                             if (url.substring(url.length() - 1).equals("#")) {
                                                 code = url.substring(url.indexOf(Constants.Keys.CODE) + Constants.Keys.CODE.length() + 1, url.length() - 1);
                                             } else {
                                                 code = url.substring(url.indexOf(Constants.Keys.CODE) + Constants.Keys.CODE.length() + 1);
                                             }

                                             DebuggIt.getInstance().getApiService().exchangeAuthCodeForToken(
                                                     code,
                                                     new JsonResponseCallback() {
                                                         @Override
                                                         public void onSuccess(JSONObject response) {
                                                             try {
                                                                 switch (DebuggIt.getInstance().getConfigType()) {
                                                                     case BITBUCKET:
                                                                         handleBitBucketLoginResponse(response);
                                                                         break;
                                                                     case GITHUB:
                                                                         handleGitHubLoginResponse(response);
                                                                         break;
                                                                 }
                                                             } catch (JSONException e) {
                                                                 e.printStackTrace();
                                                                 onException(new Exception());
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

                                             keepProgressBarVisible = true;
                                             webView.stopLoading();
                                             return true;
                                         }
                                         return false;
                                     }
                                 }

        );

        switch (DebuggIt.getInstance().getConfigType()) {
            case BITBUCKET:
                webView.loadUrl(String.format(Constants.BitBucket.LOGIN_PAGE, Constants.BitBucket.CLIENT_ID));
                break;
            case GITHUB:
                webView.loadUrl(String.format(Constants.GitHub.LOGIN_PAGE, Constants.GitHub.CLIENT_ID));
                break;
        }

    }

    private void handleBitBucketLoginResponse(JSONObject response) throws JSONException {
        DebuggIt.getInstance().saveTokens(response);
        ConfirmationDialog.newInstance(getString(R.string.br_login_successful), false).show(getChildFragmentManager(), ConfirmationDialog.TAG);
    }

    private void handleGitHubLoginResponse(JSONObject response) throws JSONException {
        DebuggIt.getInstance().saveTokens(response);
        ConfirmationDialog.newInstance(getString(R.string.br_login_successful), false).show(getChildFragmentManager(), ConfirmationDialog.TAG);
    }


}
