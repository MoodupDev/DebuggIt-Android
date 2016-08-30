package com.mooduplabs.debuggit;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Utils {

    protected static final String MEDIA_FILE_FORMAT = ".mpeg";
    private static final String API_VERSION_FORMAT = "%s (API %d)";
    private static final String APPLICATION_VERSION_FORMAT = "%s (%d)";
    private static final String MARKDOWN_BOLD = "**";
    private static final String MARKDOWN_CELL_SEPARATOR = " | ";

    protected static void putString(Context context, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(key, value);
        editor.commit();
    }

    protected static String getString(Context context, String key, String defValue) {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).getString(key, defValue);
    }

    protected static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(key, value);
        editor.commit();
    }

    protected static boolean getBoolean(Context context, String key, boolean defValue) {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).getBoolean(key, defValue);
    }

    protected static void putFloat(Context context, String key, float value) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putFloat(key, value);
        editor.commit();
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

    protected static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if(br != null) {
                try {
                    br.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    protected static String getApplicationVersion(Activity activity) {
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            return String.format(Locale.getDefault(), APPLICATION_VERSION_FORMAT, info.versionName, info.versionCode);
        } catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    protected static String getActiveFragmentName(Activity activity) {
        FragmentManager fragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
        if(fragmentManager.getBackStackEntryCount() == 0) {
            return activity.getClass().getSimpleName();
        }
        return fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
    }
    
    protected static String getDeviceInfoString(Activity activity) {
        HashMap<String, String> deviceInfo = getDeviceInfo(activity);

        StringBuilder builder = new StringBuilder();
        builder.append(" | | | ")
                .append("\n")
                .append("----|----|----|----")
                .append("\n");

        int lineCounter = 0;

        for(Map.Entry<String, String> entry : deviceInfo.entrySet()) {
            builder.append(MARKDOWN_BOLD)
                    .append(entry.getKey())
                    .append(MARKDOWN_BOLD)
                    .append(MARKDOWN_CELL_SEPARATOR)
                    .append(entry.getValue())
                    .append(lineCounter % 2 == 1 ? "\n" : MARKDOWN_CELL_SEPARATOR);
            lineCounter++;
        }

        return builder.toString();
    }

    private static HashMap<String, String> getDeviceInfo(Activity activity) {
        HashMap<String, String> deviceInfo = new HashMap<>();
        deviceInfo.put("Device", getDeviceName());
        deviceInfo.put("Android version", String.format(Locale.getDefault(), API_VERSION_FORMAT, Build.VERSION.RELEASE, Build.VERSION.SDK_INT));
        deviceInfo.put("Application version", getApplicationVersion(activity));
        deviceInfo.put("Current view", getActiveFragmentName(activity));
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
        if(resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    protected static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()) {
            if(first) {
                first = false;
            } else {
                result.append("&");
            }

            result.append(URLEncoder.encode(entry.getKey(), ApiClient.CHARSET_UTF8))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), ApiClient.CHARSET_UTF8));
        }

        return result.toString();
    }

    protected static byte[] getBytesFromFile(String filePath) {
        byte[] bytes;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileInputStream fis = new FileInputStream(new File(filePath));

            byte[] buffer = new byte[1024];
            int readLength;
            while(-1 != (readLength = fis.read(buffer))) {
                baos.write(buffer, 0, readLength);
            }
            bytes = baos.toByteArray();
        } catch(Exception e) {
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

    protected static String getUrlAsStrings(List<String> urls, boolean isMediaFile) {
        StringBuilder builder = new StringBuilder();
        if(urls != null) {
            for(String s : urls) {
                if(!isMediaFile) {
                    builder.append("![Alt text](")
                            .append(s)
                            .append(")")
                            .append("\n\n");
                } else {
                    builder.append(s)
                            .append("\n\n");
                }
            }
        }

        return builder.toString();
    }

    protected static boolean isActivityRunning(Activity activity) {
        return activity.getWindow().getDecorView().isShown();
    }

}