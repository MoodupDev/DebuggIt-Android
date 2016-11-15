package com.mooduplabs.debuggit;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.mooduplabs.debuggit.ShakeDetector.ShakeListener;

import org.json.JSONException;
import org.json.JSONObject;

public class DebuggIt {

    // region Consts

    protected static final String BUTTON_POSITION_PORTRAIT = "button_position_portrait";
    protected static final String BUTTON_POSITION_LANDSCAPE = "button_position_landscape";

    // endregion

    // region Fields

    private static DebuggIt instance;

    private Activity activity;
    private Intent screenshotIntentData;
    private View reportButton;

    private int activityOrientation;
    private boolean waitingForShake = false;
    private boolean initialized = false;
    private boolean versionChecked = false;
    private boolean versionSupported = false;
    private boolean shouldPostInitializedEvent = true;

    private ConfigType configType;

    private ApiService apiService;

    private Report report;
    private LoadingDialog screenshotLoadingDialog;

    // endregion

    // region Methods

    public static DebuggIt getInstance() {
        if(instance == null) {
            instance = new DebuggIt();
        }

        return instance;
    }

    public void initBitbucket(String clientId, String clientSecret, String repoSlug, String accountName) {
        this.apiService = new BitBucketApiService(clientId, clientSecret, repoSlug, accountName);
        init(ConfigType.BITBUCKET);
    }


    public void initJira(String host, String projectKey, boolean usesHttps) {
        this.apiService = new JiraApiService(host, projectKey, usesHttps);
        init(ConfigType.JIRA);
    }

    public void initJira(String host, String projectKey) {
        initJira(host, projectKey, true);
    }

    public void initGitHub(String repoSlug, String accountName) {
        this.apiService = new GitHubApiService(accountName, repoSlug);
        init(ConfigType.GITHUB);
    }

    private void init(ConfigType configType) {
        this.configType = configType;
        this.report = new Report();
        this.initialized = true;
    }

    public void attach(final Activity activity) {
        checkIfInitialized("attach");
        if(shouldPostInitializedEvent) {
            ApiClient.postEvent(activity, ApiClient.EventType.INITIALIZED);
            shouldPostInitializedEvent = false;
        }
        if(!versionChecked) {
            ApiClient.checkVersion(BuildConfig.VERSION_CODE, new StringResponseCallback() {
                @Override
                public void onSuccess(String response) {
                    versionChecked = versionSupported = true;
                }

                @Override
                public void onFailure(int responseCode, String errorMessage) {
                    versionChecked = true;
                    versionSupported = false;
                }

                @Override
                public void onException(Exception exception) {
                    versionChecked = versionSupported = false;
                }
            });
        }
        this.activity = activity;
        this.activityOrientation = activity.getRequestedOrientation();
        addReportButton();
        registerShakeDetector(activity);
        initScreenshotLoadingDialog();
        Thread.setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler.with(activity.getApplicationContext()));
        showWelcomeScreen();
    }

    private void showWelcomeScreen() {
        if(shouldShowWelcomeScreen()) {
            new WelcomeDialog().show(((FragmentActivity) this.activity).getSupportFragmentManager(), WelcomeDialog.TAG);
        }
    }

    private boolean shouldShowWelcomeScreen() {
        return !Utils.getBoolean(this.activity, Constants.Keys.HAS_WELCOME_SCREEN, false) && !isFragmentShown(WelcomeDialog.TAG);
    }

    private void checkIfInitialized(String callingMethodName) {
        if(!initialized) {
            throw new IllegalStateException(String.format("debugg.it must be initialized with init(...) before using %s() method", callingMethodName));
        }
    }

    public void getScreenshotPermission(int requestCode, int resultCode, Intent data) {
        checkIfInitialized("getScreenshotPermission");
        if(requestCode == ScreenshotUtils.SCREENSHOT_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                this.screenshotIntentData = data;
            } else {
                screenshotIntentData = null;
            }
        }
    }

    protected void authenticate(boolean refresh) {
        if(refresh) {
            refreshAccessToken();
            return;
        }
        if(!hasAccessToken()) {
            if(!isFragmentShown(LoginFragment.TAG)) {
                LoginFragment.newInstance().show(((FragmentActivity) activity).getSupportFragmentManager(), LoginFragment.TAG);
            }
        } else {
            applySavedTokens();
        }
    }

    protected void applySavedTokens() {
        switch(DebuggIt.getInstance().getConfigType()) {

            case BITBUCKET:
                ((BitBucketApiService) apiService).setAccessToken(Utils.getString(activity, Constants.BitBucket.ACCESS_TOKEN, ""));
                break;
            case JIRA:
                ((JiraApiService) apiService).setUsername(Utils.getString(activity, Constants.Jira.EMAIL, ""));
                ((JiraApiService) apiService).setPassword(Utils.getString(activity, Constants.Jira.PASSWORD, ""));
                break;
            case GITHUB:
                ((GitHubApiService) apiService).setAccessToken(Utils.getString(activity, Constants.GitHub.ACCESS_TOKEN, ""));
                ((GitHubApiService) apiService).setTwoFactorAuthCode(Utils.getString(activity, Constants.GitHub.TWO_FACTOR_AUTH_CODE, ""));
                break;
        }
    }

    protected void saveTokens(JSONObject response) throws JSONException {
        ((BitBucketApiService) apiService).setAccessToken(response.getString(Constants.BitBucket.ACCESS_TOKEN));
        Utils.putString(activity, Constants.BitBucket.ACCESS_TOKEN, response.getString(Constants.BitBucket.ACCESS_TOKEN));
        Utils.putString(activity, Constants.BitBucket.REFRESH_TOKEN, response.getString(Constants.BitBucket.REFRESH_TOKEN));
        waitingForShake = true;
    }

    protected Report getReport() {
        return report;
    }

    protected Activity getActivity() {
        return activity;
    }

    protected ConfigType getConfigType() {
        return configType;
    }

    protected int getActivityOrientation() {
        return activityOrientation;
    }

    protected ApiService getApiService() {
        return apiService;
    }


    private void addReportButton() {
        final FrameLayout rootLayout = (FrameLayout) activity.findViewById(android.R.id.content);
        reportButton = rootLayout.findViewById(R.id.report_button);
        if(reportButton == null) {
            reportButton = LayoutInflater.from(activity).inflate(R.layout.layout_br_report_button, rootLayout, false);
            rootLayout.addView(reportButton);
            initReportButtonOnTouchListener(rootLayout);
        }
        if(!report.getScreensUrls().isEmpty()) {
            ((ImageView) reportButton).setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.next_screenshoot, null));
        }
        initButtonPosition();
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
                                applySavedTokens();
                                takeScreenshot();
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

    private void takeScreenshot() {
        if(screenshotIntentData != null) {
            startDrawFragment();
        } else {
            ScreenshotUtils.getScreenshotPermission(activity);
        }
    }

    private void registerShakeDetector(final Activity activity) {
        ShakeDetector.getInstance().register(activity, new ShakeListener() {
            @Override
            public void shakeDetected() {
                if(Utils.isActivityRunning(activity)) {
                    if(!hasAccessToken() && !isFragmentShown(WelcomeDialog.TAG)) {
                        authenticate(false);
                    } else if(shouldShowDrawFragment()) {
                        waitingForShake = false;
                        startDrawFragment();
                    }
                }
            }

            private boolean shouldShowDrawFragment() {
                return waitingForShake
                        && !isFragmentShown(DrawFragment.TAG)
                        && !isFragmentShown(ReportFragment.TAG)
                        && !isFragmentShown(LoadingDialog.TAG)
                        && !isFragmentShown(LoginFragment.TAG)
                        && !isFragmentShown(WelcomeDialog.TAG);
            }
        });
        if(hasAccessToken()) {
            waitingForShake = true;
        }
    }

    private void refreshAccessToken() {
        Utils.putString(activity, Constants.BitBucket.ACCESS_TOKEN, "");
        apiService.refreshToken(Utils.getString(activity, Constants.BitBucket.REFRESH_TOKEN, ""), new JsonResponseCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    saveTokens(response);
                } catch(JSONException e) {
                    // ignored
                }
            }

            @Override
            public void onFailure(int responseCode, String errorMessage) {
                // do nothing
            }

            @Override
            public void onException(Exception ex) {
                // do nothing
            }
        });
    }

    private void startDrawFragment() {
        try {
            if(versionChecked && !versionSupported) {
                showUnsupportedVersionPopup();
                waitingForShake = true;
                return;
            } else if(!versionChecked) {
                showCantCheckVersionPopup();
                waitingForShake = true;
                return;
            }
            if(!isFragmentShown(DrawFragment.TAG)) {
                Utils.lockScreenRotation(activity, Utils.isOrientationLandscape(activity) ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                reportButton.setVisibility(View.GONE);
                screenshotLoadingDialog.show(((FragmentActivity) activity).getSupportFragmentManager(), LoadingDialog.TAG);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && screenshotIntentData != null) {
                    ScreenshotUtils.setNextScreenshotCanceled(false);
                    ScreenshotUtils.takeScreenshot(activity, screenshotIntentData, new ScreenshotUtils.ScreenshotListener() {
                        @Override
                        public void onScreenshotReady(Bitmap bitmap) {
                            if(bitmap != null) {
                                showDrawFragment(bitmap);
                            } else {
                                screenshotLoadingDialog.dismiss();
                                reportButton.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } else {
                    showDrawFragment(ScreenshotMaker.takeScreenshotBitmap(activity));
                }
            }
        } catch(IllegalStateException e) {
            reportButton.setVisibility(View.VISIBLE);
        }
    }

    private void initScreenshotLoadingDialog() {
        screenshotLoadingDialog = LoadingDialog.newInstance(activity.getString(R.string.br_generating_screenshot), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(screenshotIntentData != null) {
                    ScreenshotUtils.setNextScreenshotCanceled(true);
                }
                reportButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private boolean isFragmentShown(String tag) {
        return ((FragmentActivity) activity).getSupportFragmentManager().findFragmentByTag(tag) != null;
    }

    private void showDrawFragment(Bitmap bitmap) {
        screenshotLoadingDialog.dismiss();
        DrawFragment.newInstance(bitmap)
                .show(((FragmentActivity) activity).getSupportFragmentManager(), DrawFragment.TAG);
        reportButton.setVisibility(View.VISIBLE);
        waitingForShake = true;
    }

    private boolean hasAccessToken() {
        switch(DebuggIt.getInstance().getConfigType()) {

            case BITBUCKET:
                return !Utils.getString(activity, Constants.BitBucket.ACCESS_TOKEN, "").isEmpty();
            case JIRA:
                return !Utils.getString(activity, Constants.Jira.EMAIL, "").isEmpty()
                        && !Utils.getString(activity, Constants.Jira.PASSWORD, "").isEmpty();
            case GITHUB:
                return !Utils.getString(activity, Constants.GitHub.ACCESS_TOKEN, "").isEmpty();
        }
        return false;
    }

    private void showUnsupportedVersionPopup() {
        ConfirmationDialog.newInstance(activity.getString(R.string.br_unsupported_version), true)
                .show(((FragmentActivity) activity).getSupportFragmentManager(), ConfirmationDialog.TAG);
        ApiClient.postEvent(getActivity(), ApiClient.EventType.HAS_UNSUPPORTED_VERSION);
    }

    private void showCantCheckVersionPopup() {
        ConfirmationDialog.newInstance(activity.getString(R.string.br_cant_check_version), true)
                .show(((FragmentActivity) activity).getSupportFragmentManager(), ConfirmationDialog.TAG);
    }

    private DebuggIt() {
        // one instance
    }

    // endregion

    enum ConfigType {
        BITBUCKET,
        JIRA,
        GITHUB
    }
}
