package com.mooduplabs.debuggit;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    protected static final String MEDIA_FILE_FORMAT = ".mpeg";
    private static final String API_VERSION_FORMAT = "%s (API %d)";
    private static final String APPLICATION_VERSION_FORMAT = "%s (%d)";
    private static final String MARKDOWN_BOLD = "**";
    private static final String MARKDOWN_CELL_SEPARATOR = " | ";
    private static final String JIRA_BOLD = "*";
    private static final String JIRA_CELL_SEPARATOR = "|";

    protected static void putString(Context context, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(key, value);
        editor.apply();
    }

    protected static String getString(Context context, String key, String defValue) {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).getString(key, defValue);
    }

    protected static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(key, value);
        editor.apply();
    }

    protected static boolean getBoolean(Context context, String key, boolean defValue) {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).getBoolean(key, defValue);
    }

    protected static void putFloat(Context context, String key, float value) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putFloat(key, value);
        editor.apply();
    }

    protected static float getFloat(Context context, String key, float defValue) {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).getFloat(key, defValue);
    }

    protected static Bitmap getBitmapFromView(View view) {
        view.setDrawingCacheEnabled(true);

        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache());

        view.setDrawingCacheEnabled(false);
        view.destroyDrawingCache();

        return bmp;
    }

    protected static String getApplicationVersion(Activity activity) {
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            return String.format(Locale.getDefault(), APPLICATION_VERSION_FORMAT, info.versionName, info.versionCode);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    protected static String getActiveFragmentName(Activity activity) {
        FragmentManager fragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() == 0) {
            return activity.getClass().getSimpleName();
        }
        return fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
    }

    protected static String getDeviceInfoString(Activity activity) {
        HashMap<String, String> deviceInfo = getDeviceInfo(activity);

        StringBuilder builder = new StringBuilder();
        appendTableHeader(builder);
        appendDeviceInfo(deviceInfo, builder);

        return builder.toString();
    }

    private static void appendDeviceInfo(HashMap<String, String> deviceInfo, StringBuilder builder) {
        int lineCounter = 0;

        switch (DebuggIt.getInstance().getConfigType()) {
            case JIRA:
                builder.append(JIRA_CELL_SEPARATOR);
                for (Map.Entry<String, String> entry : deviceInfo.entrySet()) {
                    builder.append(JIRA_BOLD).append(entry.getKey()).append(JIRA_BOLD)
                            .append(JIRA_CELL_SEPARATOR).append(entry.getValue()).append(lineCounter % 2 == 1 ? JIRA_CELL_SEPARATOR + "\n" + JIRA_CELL_SEPARATOR : JIRA_CELL_SEPARATOR);
                    lineCounter++;
                }
                builder.deleteCharAt(builder.lastIndexOf(JIRA_CELL_SEPARATOR));
                break;
            case GITHUB:
            case BITBUCKET:
            default:
                for (Map.Entry<String, String> entry : deviceInfo.entrySet()) {
                    builder.append(MARKDOWN_BOLD)
                            .append(entry.getKey())
                            .append(MARKDOWN_BOLD)
                            .append(MARKDOWN_CELL_SEPARATOR)
                            .append(entry.getValue())
                            .append(lineCounter % 2 == 1 ? "\n" : MARKDOWN_CELL_SEPARATOR);
                    lineCounter++;
                }
                break;
        }
    }

    private static StringBuilder appendTableHeader(StringBuilder builder) {
        switch (DebuggIt.getInstance().getConfigType()) {
            case JIRA:
                builder.append("|| Key || Value || Key || Value ||")
                        .append("\n");
                break;
            case BITBUCKET:
                builder.append(" | | | ")
                        .append("\n")
                        .append("----|----|----|----")
                        .append("\n");
                break;
            case GITHUB:
                builder.append("Key | Value | Key | Value")
                        .append("\n")
                        .append("----|----|----|----")
                        .append("\n");
                break;
        }
        return builder;
    }

    private static HashMap<String, String> getDeviceInfo(Activity activity) {
        HashMap<String, String> deviceInfo = new HashMap<>();
        deviceInfo.put("Device", getDeviceName());
        deviceInfo.put("Android version", String.format(Locale.getDefault(), API_VERSION_FORMAT, Build.VERSION.RELEASE, Build.VERSION.SDK_INT));
        deviceInfo.put("Application version", getApplicationVersion(activity));
        return deviceInfo;
    }

    protected static boolean isOrientationLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    protected static void lockScreenRotation(Activity activity, int orientation) {
        activity.setRequestedOrientation(orientation);
    }

    protected static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = 0;
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    protected static byte[] getBytesFromFile(String filePath) {
        byte[] bytes;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileInputStream fis = new FileInputStream(new File(filePath));

            byte[] buffer = new byte[1024];
            int readLength;
            while (-1 != (readLength = fis.read(buffer))) {
                baos.write(buffer, 0, readLength);
            }
            bytes = baos.toByteArray();
            fis.close();
        } catch (Exception e) {
            bytes = new byte[1];
        }
        return bytes;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }

    protected static String getScreenModelsAsStrings(List<ScreenModel> screens) {
        StringBuilder builder = new StringBuilder();

        if (screens != null) {
            for (int i = 0; i < screens.size(); i++) {
                builder.append("**View:** ").append(screens.get(i).getTitle()).append("\n\n");
                appendImageLink(builder, screens.get(i).getUrl())
                        .append("\n\n");
            }
        }

        return builder.toString();
    }

    protected static String getAudioUrlsAsStrings(List<String> audioUrls) {
        StringBuilder builder = new StringBuilder();

        if (audioUrls != null) {
            for (int i = 0; i < audioUrls.size(); i++) {
                switch (DebuggIt.getInstance().getConfigType()) {
                    case JIRA:
                        builder.append("[").append("Audio ").append(i + 1).append("|")
                                .append(audioUrls.get(i)).append("]")
                                .append("\n\n");
                        break;
                    case GITHUB:
                    case BITBUCKET:
                        builder.append("[").append("Audio ").append(i + 1).append("]")
                                .append("(").append(audioUrls.get(i)).append(")")
                                .append("\n\n");
                        break;
                }
            }
        }

        return builder.toString();
    }

    private static StringBuilder appendImageLink(StringBuilder builder, String url) {
        switch (DebuggIt.getInstance().getConfigType()) {
            case JIRA:
                builder.append("!")
                        .append(url)
                        .append("!");
                break;
            case GITHUB:
            case BITBUCKET:
                builder.append("![Alt text](")
                        .append(url)
                        .append(")");
                break;
        }
        return builder;
    }

    protected static boolean isActivityRunning(Activity activity) {
        return activity.getWindow().getDecorView().isShown();
    }

    protected static String getBitbucketErrorMessage(String response, String defaultMessage) {
        try {
            JSONObject error = new JSONObject(response);
            if (error.has("error_description")) {
                return error.getString("error_description");
            }
            if (error.has("message")) {
                return error.getString("message");
            }
            return error.getJSONObject("error").getString("message");
        } catch (JSONException e) {
            Matcher matcher = Pattern.compile("<.+>").matcher(response);
            return response.isEmpty() || matcher.find() ? defaultMessage : response;
        }
    }

    protected static String convertPriorityName(String priority) {
        switch (DebuggIt.getInstance().getConfigType()) {
            case BITBUCKET:
                switch (priority) {
                    case Constants.PRIORITY_LOW:
                        return Constants.BitBucket.PRIORITY_MINOR;
                    case Constants.PRIORITY_MEDIUM:
                        return Constants.BitBucket.PRIORITY_MAJOR;
                    case Constants.PRIORITY_HIGH:
                        return Constants.BitBucket.PRIORITY_CRITICAL;
                }
            case JIRA:
            case GITHUB:
            default:
                return priority;
        }
    }

    protected static String getDateString(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

}
