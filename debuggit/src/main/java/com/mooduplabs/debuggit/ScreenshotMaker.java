/*
Copyright 2015 Josef Raska

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

package com.mooduplabs.debuggit;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND;

/**
 * Utility class to take screenshots of activity screen
 */
final class ScreenshotMaker {
    private static final String TAG = "ScreenshotMaker";

    // No instances
    private ScreenshotMaker() {
    }

    /**
     * Takes screenshot of provided activity and saves it to provided file.
     * File content will be overwritten if there is already some content.
     *
     * @param activity Activity of which the screenshot will be taken.
     * @param toFile   File where the screenshot will be saved.
     *                 If there is some content it will be overwritten
     * @throws UnableToTakeScreenshotException When there is unexpected error during taking screenshot
     */
    public static void takeScreenshot(Activity activity, final File toFile) {
        if (activity == null) {
            throw new IllegalArgumentException("Parameter activity cannot be null.");
        }

        if (toFile == null) {
            throw new IllegalArgumentException("Parameter toFile cannot be null.");
        }

        Bitmap bitmap = null;
        try {
            bitmap = takeBitmapUnchecked(activity);
            writeBitmap(bitmap, toFile);
        } catch (Exception e) {
            String message = "Unable to take screenshot to file " + toFile.getAbsolutePath()
                    + " of activity " + activity.getClass().getName();

            Log.e(TAG, message, e);
            throw new UnableToTakeScreenshotException(message, e);
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }

        Log.d(TAG, "Screenshot captured to " + toFile.getAbsolutePath());
    }


    /**
     * Takes screenshot of provided activity and puts it into bitmap.
     *
     * @param activity Activity of which the screenshot will be taken.
     * @return Bitmap of what is displayed in activity.
     * @throws UnableToTakeScreenshotException When there is unexpected error during taking screenshot
     */
    public static Bitmap takeScreenshotBitmap(Activity activity) {
        if (activity == null) {
            throw new IllegalArgumentException("Parameter activity cannot be null.");
        }

        try {
            return takeBitmapUnchecked(activity);
        } catch (Exception e) {
            String message = "Unable to take screenshot to bitmap of activity "
                    + activity.getClass().getName();

            Log.e(TAG, message, e);
            throw new UnableToTakeScreenshotException(message, e);
        }
    }

    private static Bitmap takeBitmapUnchecked(Activity activity) throws InterruptedException {
        final List<ViewRootData> viewRoots = getRootViews(activity);
        if (viewRoots.isEmpty()) {
            throw new UnableToTakeScreenshotException("Unable to capture any view data in " + activity);
        }

        int maxWidth = Integer.MIN_VALUE;
        int maxHeight = Integer.MIN_VALUE;

        for (ViewRootData viewRoot : viewRoots) {
            if (viewRoot.winFrame.right > maxWidth) {
                maxWidth = viewRoot.winFrame.right;
            }

            if (viewRoot.winFrame.bottom > maxHeight) {
                maxHeight = viewRoot.winFrame.bottom;
            }
        }

        final Bitmap bitmap = Bitmap.createBitmap(maxWidth, maxHeight, ARGB_8888);

        // We need to do it in main thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            drawRootsToBitmap(viewRoots, bitmap);
        } else {
            final CountDownLatch latch = new CountDownLatch(1);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        drawRootsToBitmap(viewRoots, bitmap);
                    } finally {
                        latch.countDown();
                    }
                }
            });

            latch.await();
        }

        return bitmap;
    }

    private static void drawRootsToBitmap(List<ViewRootData> viewRoots, Bitmap bitmap) {
        for (ViewRootData rootData : viewRoots) {
            drawRootToBitmap(rootData, bitmap);
        }
    }

    private static void drawRootToBitmap(ViewRootData config, Bitmap bitmap) {
        // now only dim supported
        if ((config.layoutParams.flags & FLAG_DIM_BEHIND) == FLAG_DIM_BEHIND) {
            Canvas dimCanvas = new Canvas(bitmap);

            int alpha = (int) (255 * config.layoutParams.dimAmount);
            dimCanvas.drawARGB(alpha, 0, 0, 0);
        }

        Canvas canvas = new Canvas(bitmap);
        canvas.translate(config.winFrame.left, config.winFrame.top);
        config.view.draw(canvas);
    }

    private static void writeBitmap(Bitmap bitmap, File toFile) throws IOException {
        OutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(toFile));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } finally {
            closeQuietly(outputStream);
        }
    }

    private static void closeQuietly(Closeable closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (IOException e) {
                // Do nothing
            }
        }
    }

    @SuppressWarnings("unchecked") // no way to check
    private static List<ViewRootData> getRootViews(Activity activity) {
        List<ViewRootData> rootViews = new ArrayList<>();

        Object globalWindowManager;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
            globalWindowManager = getFieldValue("mWindowManager", activity.getWindowManager());
        } else {
            globalWindowManager = getFieldValue("mGlobal", activity.getWindowManager());
        }
        Object rootObjects = getFieldValue("mRoots", globalWindowManager);
        Object paramsObject = getFieldValue("mParams", globalWindowManager);

        Object[] roots;
        LayoutParams[] params;

        //  There was a change to ArrayList implementation in 4.4
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            roots = ((List) rootObjects).toArray();

            List<LayoutParams> paramsList = (List<LayoutParams>) paramsObject;
            params = paramsList.toArray(new LayoutParams[paramsList.size()]);
        } else {
            roots = (Object[]) rootObjects;
            params = (LayoutParams[]) paramsObject;
        }

        for (int i = 0; i < roots.length; i++) {
            Object root = roots[i];

            View view = (View) getFieldValue("mView", root);

            // fixes https://github.com/jraska/Falcon/issues/10
            if (view == null) {
                Log.e(TAG, "null View stored as root in Global window manager, skipping");
                continue;
            }

            Object attachInfo = getFieldValue("mAttachInfo", root);
            int top = (int) getFieldValue("mWindowTop", attachInfo);
            int left = (int) getFieldValue("mWindowLeft", attachInfo);

            Rect winFrame = (Rect) getFieldValue("mWinFrame", root);
            Rect area = new Rect(left, top, left + winFrame.width(), top + winFrame.height());

            rootViews.add(new ViewRootData(view, area, params[i]));
        }

        if (rootViews.isEmpty()) {
            return Collections.emptyList();
        }

        offsetRootsTopLeft(rootViews);
        ensureDialogsAreAfterItsParentActivities(rootViews);

        return rootViews;
    }

    private static void offsetRootsTopLeft(List<ViewRootData> rootViews) {
        int minTop = Integer.MAX_VALUE;
        int minLeft = Integer.MAX_VALUE;
        for (ViewRootData rootView : rootViews) {
            if (rootView.winFrame.top < minTop) {
                minTop = rootView.winFrame.top;
            }

            if (rootView.winFrame.left < minLeft) {
                minLeft = rootView.winFrame.left;
            }
        }

        for (ViewRootData rootView : rootViews) {
            rootView.winFrame.offset(-minLeft, -minTop);
        }
    }

    // This fixes issue #11. It is not perfect solution and maybe there is another case
    // of different type of view, but it works for most common case of dialogs.
    private static void ensureDialogsAreAfterItsParentActivities(List<ViewRootData> viewRoots) {
        if (viewRoots.size() <= 1) {
            return;
        }

        for (int dialogIndex = 0; dialogIndex < viewRoots.size() - 1; dialogIndex++) {
            ViewRootData viewRoot = viewRoots.get(dialogIndex);
            if (!viewRoot.isDialogType()) {
                continue;
            }

            Activity dialogOwnerActivity = ownerActivity(viewRoot.context());
            if (dialogOwnerActivity == null) {
                // make sure we will never compare null == null
                return;
            }

            for (int parentIndex = dialogIndex + 1; parentIndex < viewRoots.size(); parentIndex++) {
                ViewRootData possibleParent = viewRoots.get(parentIndex);
                if (possibleParent.isActivityType()
                        && ownerActivity(possibleParent.context()) == dialogOwnerActivity) {
                    viewRoots.remove(possibleParent);
                    viewRoots.add(dialogIndex, possibleParent);

                    break;
                }
            }
        }
    }

    private static Activity ownerActivity(Context context) {
        Context currentContext = context;

        while (currentContext != null) {
            if (currentContext instanceof Activity) {
                return (Activity) currentContext;
            }

            if (currentContext instanceof ContextWrapper && !(currentContext instanceof Application)) {
                currentContext = ((ContextWrapper) currentContext).getBaseContext();
            } else {
                break;
            }
        }

        return null;
    }

    private static Object getFieldValue(String fieldName, Object target) {
        try {
            return getFieldValueUnchecked(fieldName, target);
        } catch (Exception e) {
            throw new UnableToTakeScreenshotException(e);
        }
    }

    private static Object getFieldValueUnchecked(String fieldName, Object target)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = findField(fieldName, target.getClass());

        field.setAccessible(true);
        return field.get(target);
    }


    private static Field findField(String name, Class clazz) throws NoSuchFieldException {
        Class currentClass = clazz;
        while (currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (name.equals(field.getName())) {
                    return field;
                }
            }

            currentClass = currentClass.getSuperclass();
        }

        throw new NoSuchFieldException("Field " + name + " not found for class " + clazz);
    }


    /**
     * Custom exception thrown if there is some exception thrown during
     * screenshot capturing to enable better client code exception handling.
     */
    public static class UnableToTakeScreenshotException extends RuntimeException {
        private UnableToTakeScreenshotException(String detailMessage) {
            super(detailMessage);
        }

        private UnableToTakeScreenshotException(String detailMessage, Exception exception) {
            super(detailMessage, extractException(exception));
        }

        private UnableToTakeScreenshotException(Exception ex) {
            super(extractException(ex));
        }

        /**
         * Method to avoid multiple wrapping. If there is already our exception,
         * just wrap the cause again
         */
        private static Throwable extractException(Exception ex) {
            if (ex instanceof UnableToTakeScreenshotException) {
                return ex.getCause();
            }

            return ex;
        }
    }

    private static class ViewRootData {
        private final View view;
        private final Rect winFrame;
        private final LayoutParams layoutParams;

        ViewRootData(View view, Rect winFrame, LayoutParams layoutParams) {
            this.view = view;
            this.winFrame = winFrame;
            this.layoutParams = layoutParams;
        }

        boolean isDialogType() {
            return layoutParams.type == LayoutParams.TYPE_APPLICATION;
        }

        boolean isActivityType() {
            return layoutParams.type == LayoutParams.TYPE_BASE_APPLICATION;
        }

        Context context() {
            return view.getContext();
        }
    }
}