package com.moodup.bugreporter;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.moodup.bugreporter.ShakeDetector.ShakeListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

public class BugReporter {

    protected static final String BUTTON_POSITION_PORTRAIT = "button_position_portrait";
    protected static final String BUTTON_POSITION_LANDSCAPE = "button_position_landscape";
    protected static final String ACCESS_TOKEN = "access_token";
    protected static final String REFRESH_TOKEN = "refresh_token";

    private static BugReporter instance;

    private Activity activity;
    private Intent screenshotIntentData;
    private View reportButton;

    private int activityOrientation;
    private boolean waitingForShake = true;

    private String clientId;
    private String clientSecret;
    private String repoSlug;
    private String accountName;
    private String accessToken;

    private Report report;

    public static BugReporter getInstance() {
        if(instance == null) {
            instance = new BugReporter();
        }

        return instance;
    }

    private BugReporter() {

    }

    public void init(String clientId, String clientSecret, String repoSlug, String accountName) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.repoSlug = repoSlug;
        this.accountName = accountName;
        this.report = new Report();
    }

    public void attach(final Activity activity) {
        this.activity = activity;
        this.activityOrientation = activity.getRequestedOrientation();
        addReportButton();
        registerShakeDetector(activity);
        ScreenshotUtils.getScreenshotPermission(activity);
    }

    private void registerShakeDetector(Activity activity) {
        ShakeDetector.getInstance().register(activity, new ShakeListener() {
            @Override
            public void shakeDetected() {
                if(waitingForShake && !isFragmentShown(DrawFragment.TAG)) {
                    showDrawFragment();
                    waitingForShake = false;
                }
            }
        });
    }

    protected void authenticate(boolean refresh) {
        if(refresh) {
            refreshAccessToken();
            return;
        }
        if(!hasAccessToken()) {
            if(!isFragmentShown(LoginDialog.TAG)) {
                LoginDialog.newInstance().show(((FragmentActivity) activity).getSupportFragmentManager(), LoginDialog.TAG);
            }
        } else {
            accessToken = Utils.getString(activity, ACCESS_TOKEN, "");
        }
    }

    private boolean hasAccessToken() {
        return !Utils.getString(activity, ACCESS_TOKEN, "").isEmpty();
    }

    private void refreshAccessToken() {
        ApiClient apiClient = new ApiClient(repoSlug, accountName, accessToken);
        Utils.putString(activity, ACCESS_TOKEN, "");
        apiClient.refreshToken(clientId, clientSecret, Utils.getString(activity, REFRESH_TOKEN, ""), new ApiClient.HttpHandler() {
            @Override
            public void done(HttpResponse data) {
                if(data.responseCode == HttpURLConnection.HTTP_OK) {
                    try {
                        saveTokens(data);
                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    protected void saveTokens(HttpResponse data) throws JSONException {
        JSONObject json = new JSONObject(data.getMessage());
        accessToken = json.getString(ACCESS_TOKEN);
        Utils.putString(activity, ACCESS_TOKEN, accessToken);
        Utils.putString(activity, REFRESH_TOKEN, json.getString(REFRESH_TOKEN));
    }

    private void addReportButton() {
        final FrameLayout rootLayout = (FrameLayout) activity.findViewById(android.R.id.content);
        reportButton = LayoutInflater.from(activity).inflate(R.layout.report_button_layout, rootLayout, false);
        initButtonPosition();
        boolean buttonAdded = rootLayout.findViewById(R.id.report_button) != null;
        if(!report.getScreensUrls().isEmpty()) {
            ((ImageView) reportButton).setImageDrawable(activity.getResources().getDrawable(R.drawable.next_screenshoot));
        }
        if(!buttonAdded) {
            rootLayout.addView(reportButton);
            initReportButtonOnTouchListener(rootLayout);
        }
    }

    private void initButtonPosition() {
        float buttonPosition = Utils.getFloat(reportButton.getContext(), Utils.isOrientationLandscape(activity) ? BUTTON_POSITION_LANDSCAPE : BUTTON_POSITION_PORTRAIT, 0);
        if(buttonPosition == 0) {
            Rect visibleFrame = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(visibleFrame);
            reportButton.setY(visibleFrame.bottom / 2);
        } else {
            reportButton.setY(buttonPosition);
        }
    }

    private void initReportButtonOnTouchListener(final FrameLayout rootLayout) {
        reportButton.setOnTouchListener(new View.OnTouchListener() {
            float dY;
            float previousY;
            boolean isMoving = false;
            final int MOVE_TOLERANCE = 5;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dY = view.getY() - event.getRawY();
                        previousY = view.getY();
                        break;

                    case MotionEvent.ACTION_UP:
                        if(!isMoving || Math.abs(previousY - view.getY()) <= MOVE_TOLERANCE) {
                            if(!hasAccessToken()) {
                                authenticate(false);
                            } else {
                                showDrawFragment();
                            }
                        }
                        isMoving = false;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        isMoving = true;
                        setNewPosition(view, event);
                        break;
                    default:
                        return false;
                }
                return true;
            }

            private void setNewPosition(View view, MotionEvent event) {
                float newY = event.getRawY() + dY;
                float buttonHeight = view.getMeasuredHeight();
                int statusBarHeight = Utils.getStatusBarHeight(view.getContext());
                newY = newY < statusBarHeight ? statusBarHeight : newY;
                newY = newY > rootLayout.getBottom() - buttonHeight - statusBarHeight ?
                        rootLayout.getBottom() - buttonHeight - statusBarHeight : newY;
                view.animate()
                        .y(newY)
                        .setDuration(0)
                        .start();
                Utils.putFloat(view.getContext(), Utils.isOrientationLandscape(activity) ? BUTTON_POSITION_LANDSCAPE : BUTTON_POSITION_PORTRAIT, newY);
            }
        });
    }

    private void showDrawFragment() {
        if(!isFragmentShown(DrawFragment.TAG)) {
            Utils.lockScreenRotation(activity, Utils.isOrientationLandscape(activity) ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            reportButton.setVisibility(View.GONE);
            final LoadingDialog dialog = LoadingDialog.newInstance(activity.getString(R.string.br_generating_screenshot));
            dialog.show(((FragmentActivity) activity).getSupportFragmentManager(), LoadingDialog.TAG);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && screenshotIntentData != null) {
                ScreenshotUtils.takeScreenshot(activity, screenshotIntentData, new ScreenshotUtils.ScreenshotListener() {
                    @Override
                    public void onScreenshotReady(Bitmap bitmap) {
                        showDrawFragment(bitmap, dialog);
                    }
                });
            } else {
                showDrawFragment(Utils.getBitmapFromView(activity.getWindow().getDecorView()), dialog);
            }
        }
    }

    private boolean isFragmentShown(String tag) {
        return ((FragmentActivity) activity).getSupportFragmentManager().findFragmentByTag(tag) != null;
    }

    private void showDrawFragment(Bitmap bitmap, LoadingDialog dialog) {
        dialog.dismiss();
        DrawFragment.newInstance(bitmap)
                .show(((FragmentActivity) activity).getSupportFragmentManager(), DrawFragment.TAG);
        reportButton.setVisibility(View.VISIBLE);
        waitingForShake = true;
    }

    protected String getAccessToken() {
        if(accessToken == null) {
            accessToken = Utils.getString(activity, ACCESS_TOKEN, "");
        }
        return accessToken;
    }

    protected String getRepoSlug() {
        return repoSlug;
    }

    protected String getAccountName() {
        return accountName;
    }

    protected Report getReport() {
        return report;
    }

    protected Activity getActivity() {
        return activity;
    }

    protected String getClientId() {
        return clientId;
    }

    protected String getClientSecret() {
        return clientSecret;
    }

    protected int getActivityOrientation() {
        return activityOrientation;
    }

    public void getScreenshotPermission(int requestCode, int resultCode, Intent data) {
        if(requestCode == ScreenshotUtils.SCREENSHOT_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                this.screenshotIntentData = data;
            } else {
                screenshotIntentData = null;
            }
        }
    }
}
