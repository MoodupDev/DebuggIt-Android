package com.moodup.bugreporter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class ScreenshotUtils {
    //region Consts

    public static final int SCREENSHOT_REQUEST_CODE = 345;
    public static final String SCREENSHOT_VIRTUAL_DISPLAY_NAME = "screenshot-virtual-display";
    public static final String SCREENSHOT_HANDLER_NAME = "ScreenshotHandler";

    //endregion

    //region Fields

    static Handler handler;

    //endregion

    //region Override Methods

    //endregion

    //region Events

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

            final ImageReader imageReader = ImageReader.newInstance(screenSize.width(), screenSize.height(), PixelFormat.RGBA_8888, 1);
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
                        final Bitmap bitmap;
                        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP || Utils.isOrientationLandscape(activity)) {
                            bitmap = getBitmapFromImageForMarshmallow(image);
                        } else {
                            bitmap = getBitmapFromImage(image);
                        }

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onScreenshotReady(bitmap);
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
    private static Bitmap getBitmapFromImage(Image image) {
        final Image.Plane[] planes = image.getPlanes();
        final Buffer buffer = planes[0].getBuffer().rewind();
        Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        return bitmap;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static Bitmap getBitmapFromImageForMarshmallow(Image image) {
        final Image.Plane[] planes = image.getPlanes();
        int width = image.getWidth();
        int height = image.getHeight();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;

        int offset = 0;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        ByteBuffer buffer = planes[0].getBuffer();
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                int pixel = 0;
                pixel |= (buffer.get(offset) & 0xff) << 16;     // R
                pixel |= (buffer.get(offset + 1) & 0xff) << 8;  // G
                pixel |= (buffer.get(offset + 2) & 0xff);       // B
                pixel |= (buffer.get(offset + 3) & 0xff) << 24; // A
                bitmap.setPixel(j, i, pixel);
                offset += pixelStride;
            }
            offset += rowPadding;
        }
        return bitmap;
    }

    private static MediaProjection initMediaProjection(Activity activity, Intent data) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaProjectionManager projectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            return projectionManager.getMediaProjection(Activity.RESULT_OK, data);
        }
        return null;
    }

    public static Bitmap createTrimmedBitmap(Bitmap bmp) {

        int imgHeight = bmp.getHeight();
        int imgWidth = bmp.getWidth();
        int smallX = 0, largeX = imgWidth, smallY = 0, largeY = imgHeight;
        int left = imgWidth, right = imgWidth, top = imgHeight, bottom = imgHeight;
        for(int i = 0; i < imgWidth; i++) {
            for(int j = 0; j < imgHeight; j++) {
                if(bmp.getPixel(i, j) != Color.TRANSPARENT) {
                    if((i - smallX) < left) {
                        left = (i - smallX);
                    }
                    if((largeX - i) < right) {
                        right = (largeX - i);
                    }
                    if((j - smallY) < top) {
                        top = (j - smallY);
                    }
                    if((largeY - j) < bottom) {
                        bottom = (largeY - j);
                    }
                }
            }
        }
        Log.d(ScreenshotUtils.class.getSimpleName(), "left:" + left + " right:" + right + " top:" + top + " bottom:" + bottom);
        bmp = Bitmap.createBitmap(bmp, left, top, imgWidth - left - right, imgHeight - top - bottom);

        return bmp;
    }

    interface ScreenshotListener {
        void onScreenshotReady(Bitmap bitmap);
    }

    //endregion


}
