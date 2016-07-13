package com.moodup.bugreporter;

import android.app.Activity;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.cloudinary.Cloudinary;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.HashMap;

public class BugReporter {
    
    public static final String BUTTON_POSITION = "button_position";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";

    private static BugReporter instance;

    private Activity activity;
    private View reportButton;
    private Cloudinary cloudinary;

    private String clientId;
    private String clientSecret;
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

    public void init(String clientId, String clientSecret, String repoSlug, String accountName) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
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

    protected void authenticate(boolean refresh) {
        if(refresh) {
            refreshAccessToken();
            return;
        }
        if (Utils.getString(activity, ACCESS_TOKEN, "").isEmpty()) {
            getBitBucketAccessToken();
        } else {
            accessToken = Utils.getString(activity, ACCESS_TOKEN, "");
        }
    }

    private void getBitBucketAccessToken() {
        ApiClient apiClient = new ApiClient(repoSlug, accountName, accessToken);
        apiClient.authorize(clientId, clientSecret, "", new ApiClient.HttpHandler() {
            @Override
            public void done(HttpResponse data) {
                if(data.responseCode == HttpURLConnection.HTTP_OK) {
                    try {
                        saveTokens(data);
                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                } else if(data.getResponseCode() < 0) {
                    // TODO: 13.07.2016 refresh token on connection back
                    ConfirmationDialog.newInstance(data.getMessage()).show(((AppCompatActivity) activity).getSupportFragmentManager(), "");
                }
            }
        });
    }

    private void refreshAccessToken() {
        ApiClient apiClient = new ApiClient(repoSlug, accountName, accessToken);
        Utils.putString(activity, ACCESS_TOKEN, "");
        apiClient.authorize(clientId, clientSecret, Utils.getString(activity, REFRESH_TOKEN, ""), new ApiClient.HttpHandler() {
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

    private void saveTokens(HttpResponse data) throws JSONException {
        JSONObject json = new JSONObject(data.getMessage());
        accessToken = json.getString(ACCESS_TOKEN);
        Utils.putString(activity, ACCESS_TOKEN, accessToken);
        Utils.putString(activity, REFRESH_TOKEN, json.getString(ApiClient.REFRESH_TOKEN));
    }

    private void addReportButton() {
        final FrameLayout rootLayout = (FrameLayout) activity.findViewById(android.R.id.content);
        reportButton = LayoutInflater.from(activity).inflate(R.layout.report_button_layout, rootLayout, false);
        boolean buttonAdded = rootLayout.findViewById(R.id.report_button) != null;
        float buttonPosition = Utils.getFloat(reportButton.getContext(), BUTTON_POSITION, 0);
        if (buttonPosition == 0) {
            Rect visibleFrame = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(visibleFrame);
            reportButton.setY(visibleFrame.bottom / 2);
        } else {
            reportButton.setY(buttonPosition);
        }
        if (!buttonAdded) {
            rootLayout.addView(reportButton);

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
                            if (!isMoving || Math.abs(previousY - view.getY()) <= MOVE_TOLERANCE) {
                                showDrawFragment();
                            }
                            isMoving = false;
                            break;

                        case MotionEvent.ACTION_MOVE:
                            isMoving = true;
                            float newY = event.getRawY() + dY;
                            float buttonHeight = view.getMeasuredHeight();
                            int statusBarHeight = getStatusBarHeight();
                            newY = newY < statusBarHeight ? statusBarHeight : newY;
                            newY = newY > rootLayout.getBottom() - buttonHeight - statusBarHeight ?
                                            rootLayout.getBottom() - buttonHeight - statusBarHeight : newY;
                            view.animate()
                                    .y(newY)
                                    .setDuration(0)
                                    .start();
                            Utils.putFloat(view.getContext(), BUTTON_POSITION, newY);
                            break;
                        default:
                            return false;
                    }
                    return true;
                }

                private int getStatusBarHeight() {
                    int resourceId = rootLayout.getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
                    int statusBarHeight = 0;
                    if (resourceId > 0) {
                        statusBarHeight = rootLayout.getContext().getResources().getDimensionPixelSize(resourceId);
                    }
                    return statusBarHeight;
                }
            });
        }
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

    protected Activity getActivity() {
        return activity;
    }
}
