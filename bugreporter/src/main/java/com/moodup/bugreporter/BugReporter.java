package com.moodup.bugreporter;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.cloudinary.Cloudinary;

import java.util.HashMap;

import butterknife.ButterKnife;

public class BugReporter {

    private static BugReporter instance;

    private Activity activity;
    private View reportButton;
    private Cloudinary cloudinary;

    private String clientId;
    private String repoSlug;
    private String accountName;
    private String accessToken;

    private Report report;

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
        this.cloudinary = new Cloudinary(getCloudinaryConfig());
        this.report = new Report();
    }

    private HashMap<String, String> getCloudinaryConfig() {
        HashMap<String, String> config = new HashMap<>();

        config.put("cloud_name", "db9nesbif");
        config.put("api_key", "235172213685627");
        config.put("api_secret", "HyLIsCmPHA2MVuetbmV_t_YZa2M");

        return config;
    }

    public void attach(Activity activity) {
        this.activity = activity;
        authenticate(false);
        addReportButton();
    }

    protected void authenticate(final boolean refresh) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
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

                    Map<String, String> extraHeaders = new HashMap<>();
                    extraHeaders.put("Referer", BitBucket.REFERER_URL);

                    webView.loadUrl(String.format(BitBucket.OAUTH_URL, clientId), extraHeaders);
                } else {
                    accessToken = Utils.getString(activity, "accessToken", "");
                }
            }
        });
    }

    private void addReportButton() {
        FrameLayout rootLayout = ButterKnife.findById(activity, android.R.id.content);

        reportButton = LayoutInflater.from(activity).inflate(R.layout.report_button_layout, rootLayout, false);
        rootLayout.addView(reportButton);

        reportButton.setOnTouchListener(new View.OnTouchListener() {
            float dY;
            float previousY;
            boolean isMoving = false;
            final int MOVE_TOLERANCE = 5;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dY = view.getY() - event.getRawY();
                        previousY = view.getY();
                        break;

                    case MotionEvent.ACTION_UP:
                        if (!isMoving || Math.abs(previousY - view.getY()) <= MOVE_TOLERANCE) {
                            showDrawFragment();
                        }
                        isMoving = false;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        isMoving = true;
                        view.animate()
                                .y(event.getRawY() + dY)
                                .setDuration(0)
                                .start();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }

    public void detach() {
        activity = null;
    }

    protected void showDrawFragment() {
        new DrawFragment().show(((AppCompatActivity) activity).getSupportFragmentManager(), DrawFragment.TAG);
    }

    protected String getAccessToken() {
        return accessToken;
    }

    protected String getRepoSlug() {
        return repoSlug;
    }

    protected String getAccountName() {
        return accountName;
    }

    protected Cloudinary getCloudinary() {
        return cloudinary;
    }

    protected Report getReport() {
        return report;
    }
}
