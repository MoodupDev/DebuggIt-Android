package com.mooduplabs.debuggit;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class ScreenshotUtils {
    //region Consts

    public static final int SCREENSHOT_REQUEST_CODE = 345;
    public static final String SCREENSHOT_VIRTUAL_DISPLAY_NAME = "screenshot-virtual-display";
    public static final String SCREENSHOT_HANDLER_NAME = "ScreenshotHandler";

    private static final float[] BGR_TO_RGB_COLOR_TRANSFORM = {
        0,  0,  1f, 0,  0,
        0,  1f, 0,  0,  0,
        1f, 0,  0,  0,  0,
        0,  0,  0,  1f, 0
    };

    //endregion

    //region Fields

    static Handler handler;
    private static boolean nextScreenshotCanceled;

    //endregion

    //region Methods

    protected static Rect getScreenSize(Activity activity) {
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect;
    }

    protected static void getScreenshotPermission(Activity activity) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaProjectionManager projectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            activity.startActivityForResult(projectionManager.createScreenCaptureIntent(), SCREENSHOT_REQUEST_CODE);
        }
    }

    protected static void takeScreenshot(final Activity activity, Intent data, final ScreenshotListener listener) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final MediaProjection projection;
            try {
                projection = initMediaProjection(activity, data);
            } catch(IllegalStateException e) {
                // get next screenshot
                return;
            }
            final Rect screenSize = getScreenSize(activity);
            initHandler();

            final ImageReader imageReader = ImageReader.newInstance(screenSize.width(), screenSize.height(), PixelFormat.RGBA_8888, 5);
            if(projection != null) {
                projection.createVirtualDisplay(
                        SCREENSHOT_VIRTUAL_DISPLAY_NAME,
                        screenSize.width(),
                        screenSize.height(),
                        activity.getResources().getDisplayMetrics().densityDpi,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                        imageReader.getSurface(),
                        null,
                        handler
                );
            }
            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {

                @Override
                public void onImageAvailable(ImageReader reader) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Image image = imageReader.acquireLatestImage();
                        
                        final Bitmap bitmap = trimBitmap(getBitmap(image));

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(!nextScreenshotCanceled) {
                                    listener.onScreenshotReady(bitmap);
                                } else {
                                    listener.onScreenshotReady(null);
                                }
                                nextScreenshotCanceled = false;
                            }
                        });
                        image.close();
                        if(projection != null) {
                            projection.stop();
                            imageReader.close();
                        }
                    }
                }

            }, handler);
        }
    }

    private static void initHandler() {
        HandlerThread thread = new HandlerThread(SCREENSHOT_HANDLER_NAME) {
            @Override
            public void run() {
                Looper.prepare();
                handler = new Handler();
                Looper.loop();
            }
        };
        thread.start();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static Bitmap getBitmap(Image image) {
        final Image.Plane[] planes = image.getPlanes();
        int width = image.getWidth();
        int height = image.getHeight();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        ByteBuffer buffer = planes[0].getBuffer();
        IntBuffer intBuffer = buffer.asIntBuffer();
        int[] pixels = new int[intBuffer.capacity()];
        intBuffer.get(pixels);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix(BGR_TO_RGB_COLOR_TRANSFORM);
        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorFilter);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, rowStride / pixelStride, 0, 0, width, height);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return bitmap;
    }

    private static MediaProjection initMediaProjection(Activity activity, Intent data) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaProjectionManager projectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            return projectionManager.getMediaProjection(Activity.RESULT_OK, data);
        }
        return null;
    }

    protected static void setNextScreenshotCanceled(boolean canceled) {
        nextScreenshotCanceled = canceled;
    }

    private static Bitmap trimBitmap(Bitmap bitmap) {
        int imgHeight = bitmap.getHeight();
        int imgWidth = bitmap.getWidth();

        //TRIM WIDTH - LEFT
        int startWidth = 0;
        for(int x = 0; x < imgWidth; x++) {
            if(startWidth == 0) {
                for(int y = 0; y < imgHeight; y++) {
                    if(bitmap.getPixel(x, y) != Color.TRANSPARENT) {
                        startWidth = x;
                        break;
                    }
                }
            } else break;
        }

        //TRIM WIDTH - RIGHT
        int endWidth = 0;
        for(int x = imgWidth - 1; x >= 0; x--) {
            if(endWidth == 0) {
                for(int y = 0; y < imgHeight; y++) {
                    if(bitmap.getPixel(x, y) != Color.TRANSPARENT) {
                        endWidth = x;
                        break;
                    }
                }
            } else break;
        }

        //TRIM HEIGHT - TOP
        int startHeight = 0;
        for(int y = 0; y < imgHeight; y++) {
            if(startHeight == 0) {
                for(int x = 0; x < imgWidth; x++) {
                    if(bitmap.getPixel(x, y) != Color.TRANSPARENT) {
                        startHeight = y;
                        break;
                    }
                }
            } else break;
        }

        //TRIM HEIGHT - BOTTOM
        int endHeight = 0;
        for(int y = imgHeight - 1; y >= 0; y--) {
            if(endHeight == 0) {
                for(int x = 0; x < imgWidth; x++) {
                    if(bitmap.getPixel(x, y) != Color.TRANSPARENT) {
                        endHeight = y;
                        break;
                    }
                }
            } else break;
        }

        return Bitmap.createBitmap(
                bitmap,
                startWidth,
                startHeight,
                endWidth - startWidth,
                endHeight - startHeight
        );
    }

    protected static void trimBitmap(final Activity activity, final Bitmap bitmap, final ScreenshotListener listener) {
        if(handler == null) {
            handler = new Handler();
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                final Bitmap trimmed = trimBitmap(bitmap);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onScreenshotReady(trimmed);
                    }
                });
            }
        });
    }

    //endregion

    interface ScreenshotListener {
        void onScreenshotReady(Bitmap bitmap);
    }
}
