package com.mooduplabs.debuggit;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    //region Consts

    private static final long MB_IN_BYTES = 1024 * 1024;

    //endregion

    //region Fields

    private static UncaughtExceptionHandler instance;
    private Thread.UncaughtExceptionHandler defaultHandler;
    private Context context;

    //endregion

    //region Override Methods

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.d("Stack trace", getStackTrace(ex));
        Log.d("Other threads", getStackTraceFromEveryThread());
        ActivityManager.MemoryInfo mi = getMemoryInfo();
        Log.d("Free RAM", humanReadableByteCount(mi.availMem, false));
        Log.d("Total RAM", humanReadableByteCount(mi.totalMem, false));
        Log.d("Free space", humanReadableByteCount(freeInternalMemory(), false));
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
        Writer stackTrace = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stackTrace);
        throwable.printStackTrace(printWriter);
        return throwable.toString();
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

    private long bytesToMegabytes(long bytes) {
        return bytes / MB_IN_BYTES;
    }

    private String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
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
