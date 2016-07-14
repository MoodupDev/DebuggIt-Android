package com.moodup.bugreporter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jaredrummler.android.device.DeviceName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static final String MEDIA_FILE_FORMAT = ".mpeg";

    protected static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("#")[1].split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

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
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    protected static String getApplicationVersion(Activity activity) {
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            return String.format("%s (%d)", info.versionName, info.versionCode);
        } catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    protected static String getActiveFragmentName(Activity activity) {
        FragmentManager fragmentManager = ((AppCompatActivity) activity).getSupportFragmentManager();
        if(fragmentManager.getBackStackEntryCount() == 0) {
            return "";
        }
        return fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
    }

    // TODO: 31.05.2016 refactor
    protected static String getDeviceInfo(Activity activity) {
        StringBuilder builder = new StringBuilder();
        String activeFragmentName = getActiveFragmentName(activity);
        builder.append("\n  |  |  | \n")
                .append("--------|--------|------|-----\n")
                .append("**Device** | ")
                .append(DeviceName.getDeviceName())
                .append(" | ")
                .append("**Android version** | ")
                .append(String.format("%s (API %d)", Build.VERSION.RELEASE, Build.VERSION.SDK_INT))
                .append("\n")
                .append("**App version** | ")
                .append(getApplicationVersion(activity))
                .append(" | ")
                .append("**Current view** | ")
                .append(activeFragmentName.isEmpty() ? activity.getClass().getSimpleName() : activeFragmentName)
                .append('\n');
        return builder.toString();
    }

    protected static boolean isOrientationLandscape(Activity activity) {
        return activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    protected static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = 0;
        if(resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }
}
