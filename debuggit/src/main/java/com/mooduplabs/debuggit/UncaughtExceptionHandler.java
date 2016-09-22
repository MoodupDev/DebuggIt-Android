package com.mooduplabs.debuggit;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.text.format.Formatter;
import android.util.Log;

import java.util.Map;
import java.util.Set;

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    //region Consts

    //endregion

    //region Fields

    private static UncaughtExceptionHandler instance;
    private Thread.UncaughtExceptionHandler defaultHandler;
    private Context context;

    //endregion

    //region Override Methods

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ApiClient.postEvent(context, ApiClient.EventType.APP_CRASHED);
        Log.d("Stack trace", getStackTrace(ex));
        Log.d("Other threads", getStackTraceFromEveryThread());
        ActivityManager.MemoryInfo mi = getMemoryInfo();
        Log.d("Free RAM", humanReadableSize(mi.availMem));
        Log.d("Total RAM", humanReadableSize(mi.totalMem));
        Log.d("Free space", humanReadableSize(freeInternalMemory()));
        defaultHandler.uncaughtException(thread, ex);
    }

    //endregion

    //region Methods

    public static UncaughtExceptionHandler with(Context context) {
        if(instance == null) {
            instance = new UncaughtExceptionHandler();
        }
        if(instance.context == null) {
            instance.context = context;
        }
        return instance;
    }

    private UncaughtExceptionHandler() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @NonNull
    private String getStackTrace(StackTraceElement[] stack, String indent) {
        StringBuilder builder = new StringBuilder();
        for(StackTraceElement stackTraceElement : stack) {
            builder.append(indent).append(stackTraceElement.toString()).append("\n");
        }
        return builder.toString();
    }

    @NonNull
    private String getStackTrace(StackTraceElement[] stack) {
        return getStackTrace(stack, "\t");
    }

    private String getStackTrace(Throwable throwable) {
        return Log.getStackTraceString(throwable);
    }

    @NonNull
    private String getStackTraceFromEveryThread() {
        StringBuilder builder = new StringBuilder();
        Set<Map.Entry<Thread, StackTraceElement[]>> entries = Thread.getAllStackTraces().entrySet();
        for(Map.Entry<Thread, StackTraceElement[]> entry : entries) {
            builder.append(entry.getKey()).append('\n');
            builder.append(getStackTrace(entry.getValue())).append('\n');
        }
        return builder.toString();
    }

    private ActivityManager.MemoryInfo getMemoryInfo() {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo;
    }

    private String humanReadableSize(long bytes) {
        return Formatter.formatFileSize(context, bytes);
    }

    private long freeInternalMemory() {
        StatFs stats = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return stats.getAvailableBlocksLong() * (stats.getBlockSizeLong());
        }
        return (stats.getAvailableBlocks() * (long) stats.getBlockSize());
    }

    //endregion


}
