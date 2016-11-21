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

import java.lang.ref.WeakReference;

public class DebuggIt {

    // region Consts

    protected static final String BUTTON_POSITION_PORTRAIT = "button_position_portrait";
    protected static final String BUTTON_POSITION_LANDSCAPE = "button_position_landscape";

    // endregion

    // region Fields

    private static DebuggIt instance;

    private WeakReference<Activity> activity;
    private Intent screenshotIntentData;
    private WeakReference<View> reportButton;

    private int activityOrientation;
    private boolean waitingForShake = false;
    private boolean initialized = false;
    private boolean versionChecked = false;
    private boolean versionSupported = false;
    private boolean shouldPostInitializedEvent = true;
    private boolean recordingEnabled = false;

    private ConfigType configType;

    private ApiService apiService;

    private Report report;
    private LoadingDialog screenshotLoadingDialog;

    // endregion

    // region Methods

    public static DebuggIt getInstance() {
        if (instance == null) {
            instance = new DebuggIt();
        }

        return instance;
    }

    public void setRecordingEnabled(boolean enabled) {
        recordingEnabled = enabled;
    }

    protected boolean isRecordingEnabled() {
        return recordingEnabled;
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

    public void initGitHub(String clientId, String clientSecret, String repoSlug, String accountName) {
        this.apiService = new GitHubApiService(clientId, clientSecret, accountName, repoSlug);
        init(ConfigType.GITHUB);
    }

    private void init(ConfigType configType) {
        this.configType = configType;
        this.report = new Report();
        this.initialized = true;
    }

    public void attach(final Activity activity) {
        checkIfInitialized("attach");
        if (shouldPostInitializedEvent) {
            ApiClient.postEvent(activity, ApiClient.EventType.INITIALIZED);
            shouldPostInitializedEvent = false;
        }
        if (!versionChecked) {
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
        this.activity = new WeakReference<>(activity);
        this.activityOrientation = activity.getRequestedOrientation();
        addReportButton();
        registerShakeDetector(activity);
        initScreenshotLoadingDialog();
        Thread.setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler.with(activity.getApplicationContext()));
        showWelcomeScreen();
    }

    private void showWelcomeScreen() {
        if (shouldShowWelcomeScreen()) {
            new WelcomeDialog().show(((FragmentActivity) getActivity()).getSupportFragmentManager(), WelcomeDialog.TAG);
        }
    }

    private boolean shouldShowWelcomeScreen() {
        return !Utils.getBoolean(getActivity(), Constants.Keys.HAS_WELCOME_SCREEN, false) && !isFragmentShown(WelcomeDialog.TAG);
    }

    private void checkIfInitialized(String callingMethodName) {
        if (!initialized) {
            throw new IllegalStateException(String.format("debugg.it must be initialized with init(...) before using %s() method", callingMethodName));
        }
    }

    public void getScreenshotPermission(int requestCode, int resultCode, Intent data) {
        checkIfInitialized("getScreenshotPermission");
        if (requestCode == ScreenshotUtils.SCREENSHOT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                this.screenshotIntentData = data;
            } else {
                screenshotIntentData = null;
            }
        }
    }

    protected void authenticate(boolean refresh) {
        if (refresh) {
            refreshAccessToken();
            return;
        }
        if (!hasAccessToken()) {
            if (!isFragmentShown(LoginFragment.TAG)) {
                LoginFragment.newInstance().show(((FragmentActivity) getActivity()).getSupportFragmentManager(), LoginFragment.TAG);
            }
        } else {
            applySavedTokens();
        }
    }

    protected void applySavedTokens() {
        switch (DebuggIt.getInstance().getConfigType()) {

            case BITBUCKET:
                ((BitBucketApiService) apiService).setAccessToken(Utils.getString(getActivity(), Constants.BitBucket.ACCESS_TOKEN, ""));
                break;
            case JIRA:
                ((JiraApiService) apiService).setUsername(Utils.getString(getActivity(), Constants.Jira.EMAIL, ""));
                ((JiraApiService) apiService).setPassword(Utils.getString(getActivity(), Constants.Jira.PASSWORD, ""));
                break;
            case GITHUB:
                ((GitHubApiService) apiService).setAccessToken(Utils.getString(getActivity(), Constants.GitHub.GITHUB_ACCESS_TOKEN, ""));
                ((GitHubApiService) apiService).setTwoFactorAuthCode(Utils.getString(getActivity(), Constants.GitHub.TWO_FACTOR_AUTH_CODE, ""));
                break;
        }
    }

    protected void saveTokens(JSONObject response) throws JSONException {
        ((BitBucketApiService) apiService).setAccessToken(response.getString(Constants.BitBucket.ACCESS_TOKEN));
        Utils.putString(getActivity(), Constants.BitBucket.ACCESS_TOKEN, response.getString(Constants.BitBucket.ACCESS_TOKEN));
        Utils.putString(getActivity(), Constants.BitBucket.REFRESH_TOKEN, response.getString(Constants.BitBucket.REFRESH_TOKEN));
        waitingForShake = true;
    }

    protected void saveToken(String accessToken) {
        switch (DebuggIt.getInstance().getConfigType()) {

            case BITBUCKET:
                ((BitBucketApiService) apiService).setAccessToken(accessToken);
                Utils.putString(getActivity(), Constants.BitBucket.ACCESS_TOKEN, accessToken);
                break;
            case GITHUB:
                ((GitHubApiService) apiService).setAccessToken(accessToken);
                Utils.putString(getActivity(), Constants.GitHub.GITHUB_ACCESS_TOKEN, accessToken);
                break;
        }

        waitingForShake = true;
    }

    protected Report getReport() {
        return report;
    }

    protected Activity getActivity() {
        return activity.get();
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

    private View getReportButton() {
        return reportButton.get();
    }


    private void addReportButton() {
        final FrameLayout rootLayout = (FrameLayout) getActivity().findViewById(android.R.id.content);
        reportButton = new WeakReference<>(rootLayout.findViewById(R.id.report_button));
        if (getReportButton() == null) {
            reportButton = new WeakReference<>(LayoutInflater.from(getActivity()).inflate(R.layout.layout_br_report_button, rootLayout, false));
            rootLayout.addView(getReportButton());
            initReportButtonOnTouchListener(rootLayout);
        }
        if (!report.getScreensUrls().isEmpty()) {
            ((ImageView) getReportButton()).setImageDrawable(ResourcesCompat.getDrawable(getActivity().getResources(), R.drawable.next_screenshoot, null));
        }
        initButtonPosition();
    }

    private void initButtonPosition() {
        float buttonPosition = Utils.getFloat(getReportButton().getContext(), Utils.isOrientationLandscape(getActivity()) ? BUTTON_POSITION_LANDSCAPE : BUTTON_POSITION_PORTRAIT, 0);
        if (buttonPosition == 0) {
            Rect visibleFrame = new Rect();
            getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(visibleFrame);
            getReportButton().setY(visibleFrame.bottom / 2);
        } else {
            getReportButton().setY(buttonPosition);
        }
    }

    private void initReportButtonOnTouchListener(final FrameLayout rootLayout) {
        getReportButton().setOnTouchListener(new View.OnTouchListener() {
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
                            if (!hasAccessToken()) {
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
                Utils.putFloat(view.getContext(), Utils.isOrientationLandscape(getActivity()) ? BUTTON_POSITION_LANDSCAPE : BUTTON_POSITION_PORTRAIT, newY);
            }
        });
    }

    private void takeScreenshot() {
        if (screenshotIntentData != null) {
            startDrawFragment();
        } else {
            ScreenshotUtils.getScreenshotPermission(getActivity());
        }
    }

    private void registerShakeDetector(final Activity activity) {
        ShakeDetector.getInstance().register(activity, new ShakeListener() {
            @Override
            public void shakeDetected() {
                if (Utils.isActivityRunning(activity)) {
                    if (!hasAccessToken() && !isFragmentShown(WelcomeDialog.TAG)) {
                        authenticate(false);
                    } else if (shouldShowDrawFragment()) {
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
        if (hasAccessToken()) {
            waitingForShake = true;
        }
    }

    private void refreshAccessToken() {
        Utils.putString(getActivity(), Constants.BitBucket.ACCESS_TOKEN, "");
        apiService.refreshToken(Utils.getString(getActivity(), Constants.BitBucket.REFRESH_TOKEN, ""), new JsonResponseCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    saveTokens(response);
                } catch (JSONException e) {
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
            if (versionChecked && !versionSupported) {
                showUnsupportedVersionPopup();
                waitingForShake = true;
                return;
            } else if (!versionChecked) {
                showCantCheckVersionPopup();
                waitingForShake = true;
                return;
            }
            if (!isFragmentShown(DrawFragment.TAG)) {
                Utils.lockScreenRotation(getActivity(), Utils.isOrientationLandscape(getActivity()) ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                getReportButton().setVisibility(View.GONE);
                screenshotLoadingDialog.show(((FragmentActivity) getActivity()).getSupportFragmentManager(), LoadingDialog.TAG);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && screenshotIntentData != null) {
                    ScreenshotUtils.setNextScreenshotCanceled(false);
                    ScreenshotUtils.takeScreenshot(getActivity(), screenshotIntentData, new ScreenshotUtils.ScreenshotListener() {
                        @Override
                        public void onScreenshotReady(Bitmap bitmap) {
                            if (bitmap != null) {
                                showDrawFragment(bitmap);
                            } else {
                                screenshotLoadingDialog.dismiss();
                                getReportButton().setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } else {
                    showDrawFragment(ScreenshotMaker.takeScreenshotBitmap(getActivity()));
                }
            }
        } catch (IllegalStateException e) {
            getReportButton().setVisibility(View.VISIBLE);
        }
    }

    private void initScreenshotLoadingDialog() {
        screenshotLoadingDialog = LoadingDialog.newInstance(getActivity().getString(R.string.br_generating_screenshot), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (screenshotIntentData != null) {
                    ScreenshotUtils.setNextScreenshotCanceled(true);
                }
                getReportButton().setVisibility(View.VISIBLE);
            }
        });
    }

    private boolean isFragmentShown(String tag) {
        return ((FragmentActivity) getActivity()).getSupportFragmentManager().findFragmentByTag(tag) != null;
    }

    private void showDrawFragment(Bitmap bitmap) {
        screenshotLoadingDialog.dismiss();
        DrawFragment.newInstance(bitmap)
                .show(((FragmentActivity) getActivity()).getSupportFragmentManager(), DrawFragment.TAG);
        getReportButton().setVisibility(View.VISIBLE);
        waitingForShake = true;
    }

    private boolean hasAccessToken() {
        switch (DebuggIt.getInstance().getConfigType()) {

            case BITBUCKET:
                return !Utils.getString(getActivity(), Constants.BitBucket.ACCESS_TOKEN, "").isEmpty();
            case JIRA:
                return !Utils.getString(getActivity(), Constants.Jira.EMAIL, "").isEmpty()
                        && !Utils.getString(getActivity(), Constants.Jira.PASSWORD, "").isEmpty();
            case GITHUB:
                return !Utils.getString(getActivity(), Constants.GitHub.GITHUB_ACCESS_TOKEN, "").isEmpty();
        }
        return false;
    }

    private void showUnsupportedVersionPopup() {
        ConfirmationDialog.newInstance(getActivity().getString(R.string.br_unsupported_version), true)
                .show(((FragmentActivity) getActivity()).getSupportFragmentManager(), ConfirmationDialog.TAG);
        ApiClient.postEvent(getActivity(), ApiClient.EventType.HAS_UNSUPPORTED_VERSION);
    }

    private void showCantCheckVersionPopup() {
        ConfirmationDialog.newInstance(getActivity().getString(R.string.br_cant_check_version), true)
                .show(((FragmentActivity) getActivity()).getSupportFragmentManager(), ConfirmationDialog.TAG);
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
