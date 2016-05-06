package com.moodup.bugreporter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

public class Utils {

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

    protected static Bitmap getBitmapFromView(View view) {
        view.setDrawingCacheEnabled(true);

        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache());

        view.setDrawingCacheEnabled(false);
        view.destroyDrawingCache();

        return bmp;
    }
}
