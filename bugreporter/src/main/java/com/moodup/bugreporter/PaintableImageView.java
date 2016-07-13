package com.moodup.bugreporter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.ArrayList;

public class PaintableImageView extends ImageView {

    private static final float MINP = 0.25f;
    private static final float MAXP = 0.75f;
    private static final float TOUCH_TOLERANCE = 4;
    public static final int GROUP_HORIZONTAL = 2;
    public static final int GROUP_VERTICAL = 1;

    public static final int TYPE_FREE_DRAW = 0;
    public static final int TYPE_RECTANGLE_DRAW = 1;

    private Point[] points;
    private int groupId = -1;
    private int cornerId = 0;
    private ArrayList<Corner> corners = new ArrayList<>();

    private ArrayList<Path> pathHistory = new ArrayList<>();

    private ArrayList<Rectangle> rectanglesHistory = new ArrayList<>();

    private int type;
    private Bitmap bitmap;
    private Canvas canvas;
    private Path path;
    private Paint bitmapPaint;
    private Paint paint;
    private float x, y;

    private ArrayList<Integer> lastDrawings = new ArrayList<>();

    public PaintableImageView(Context context) {
        super(context);
        init(context);
    }

    public PaintableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PaintableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        path = new Path();
        bitmapPaint = new Paint(Paint.DITHER_FLAG);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(context.getResources().getDimensionPixelSize(R.dimen.draw_line_width));

        points = new Point[4];
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(0x00FFFFFF);
        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        canvas.drawPath(path, paint);

        if (points[3] == null)
            return;

        int left, top, right, bottom;
        left = points[0].x;
        top = points[0].y;
        right = points[0].x;
        bottom = points[0].y;
        for(int i = 0; i < points.length; i++) {
            left = left > points[i].x ? points[i].x : left;
            top = top > points[i].y ? points[i].y : top;
            right = right < points[i].x ? points[i].x : right;
            bottom = bottom < points[i].y ? points[i].y : bottom;
        }

        canvas.drawRect(
                left + corners.get(0).getCornerImageWidth() / 2,
                top + corners.get(0).getCornerImageWidth() / 2,
                right + corners.get(2).getCornerImageWidth() / 2,
                bottom + corners.get(2).getCornerImageWidth() / 2, paint);

        for (int i = 0; i < corners.size(); i ++) {
            Corner corner = corners.get(i);
            canvas.drawBitmap(corner.getBitmap(), corner.getX(), corner.getY(),
                    paint);
        }

    }

    private void freeDrawTouchStart(float x, float y) {
        path.reset();
        path.moveTo(x, y);
        this.x = x;
        this.y = y;
    }

    private void freeDrawTouchMove(float x, float y) {
        float dx = Math.abs(x - this.x);
        float dy = Math.abs(y - this.y);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path.quadTo(this.x, this.y, (x + this.x) / 2, (y + this.y) / 2);
            this.x = x;
            this.y = y;
        }
    }

    protected void clear() {
        clearRectangle();

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        invalidate();
    }

    private void freeDrawTouchUp() {
        path.lineTo(x, y);
        // commit the path to our offscreen
        canvas.drawPath(path, paint);
        // kill this so we don't double draw
        pathHistory.add(path);
        path = new Path();
        lastDrawings.add(TYPE_FREE_DRAW);
    }

    protected void previousDrawing() {
        clear();
        if(pathHistory.size() > 0 || rectanglesHistory.size() > 0) {
            switch(lastDrawings.get(lastDrawings.size() - 1)) {
                case TYPE_FREE_DRAW:
                    pathHistory.remove(pathHistory.size() - 1);
                    for(Path pathToDraw : pathHistory) {
                        canvas.drawPath(pathToDraw, paint);
                    }
                    for(Rectangle rectangle : rectanglesHistory) {
                        canvas.drawRect(
                                rectangle.left,
                                rectangle.top,
                                rectangle.right,
                                rectangle.bottom,
                                paint
                        );
                    }
                    invalidate();
                    break;
                case TYPE_RECTANGLE_DRAW:
                    rectanglesHistory.remove(rectanglesHistory.size() - 1);
                    for(Rectangle rectangle : rectanglesHistory) {
                        canvas.drawRect(
                                rectangle.left,
                                rectangle.top,
                                rectangle.right,
                                rectangle.bottom,
                                paint
                        );
                    }
                    for(Path pathToDraw : pathHistory) {
                        canvas.drawPath(pathToDraw, paint);
                    }
                    invalidate();
                    break;
                default:
                    break;
            }
            lastDrawings.remove(lastDrawings.size() -  1);
        } else {
            lastDrawings.clear();
            rectanglesHistory.clear();
            pathHistory.clear();
        }
    }

    private Bitmap getBitmapFromCanvas() {
        return this.getDrawingCache();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled()) {
            float x = event.getX();
            float y = event.getY();

            if (type == TYPE_FREE_DRAW) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        freeDrawTouchStart(x, y);
                        invalidate();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        freeDrawTouchMove(x, y);
                        invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        freeDrawTouchUp();
                        invalidate();
                        break;
                }
            } else if (type == TYPE_RECTANGLE_DRAW) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        rectanglesDrawTouchStart(x, y);
                        invalidate();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        rectanglesDrawTouchMove(x, y);
                        invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        rectanglesDrawTouchUp();
                        invalidate();
                        break;
                }
            }
            return true;
        }
        return false;
    }

    private void rectanglesDrawTouchUp() {

    }

    private void rectanglesDrawTouchMove(float x, float y) {
        if (cornerId > -1 && cornerId < corners.size()) {
            int cornerImageSize = corners.get(0).getCornerImageWidth();

            x = x > bitmap.getWidth() - cornerImageSize ? bitmap.getWidth() - cornerImageSize : x;
            x = x < 0 ? 0 : x;
            y = y > bitmap.getHeight() - cornerImageSize ? bitmap.getHeight() - cornerImageSize : y;
            y = y < 0 ? 0 : y;

            corners.get(cornerId).setX((int) x);
            corners.get(cornerId).setY((int) y);

            if (groupId == GROUP_VERTICAL) {
                corners.get(1).setX(corners.get(0).getX());
                corners.get(1).setY(corners.get(2).getY());
                corners.get(3).setX(corners.get(2).getX());
                corners.get(3).setY(corners.get(0).getY());
            } else if (groupId == GROUP_HORIZONTAL) {
                corners.get(0).setX(corners.get(1).getX());
                corners.get(0).setY(corners.get(3).getY());
                corners.get(2).setX(corners.get(3).getX());
                corners.get(2).setY(corners.get(1).getY());
            }
        }
    }

    private void rectanglesDrawTouchStart(float x, float y) {
        if (points[0] == null) {
            initPoints(x, y);
            initCorners();
        } else {
            cornerId = -1;
            groupId = -1;
            for(Corner corner : corners) {
                // check if inside the bounds of the corner
                if (isNearCorner(x, y, corner)) {

                    cornerId = corner.getID();
                    if (cornerId == 1 || cornerId == 3) {
                        groupId = GROUP_HORIZONTAL;
                    } else {
                        groupId = GROUP_VERTICAL;
                    }
                    break;
                } else if(!isNearCorners(x,y)) {
                    drawRectangle();
                    clearRectangle();
                    lastDrawings.add(TYPE_RECTANGLE_DRAW);
                    break;
                }
            }
        }
    }

    private void clearRectangle() {
        points = new Point[4];
        corners = new ArrayList<>();
        Corner.count = 0;
    }

    private boolean isNearCorner(float x, float y, Corner corner) {
        int centerX = corner.getX() + corner.getCornerImageWidth();
        int centerY = corner.getY() + corner.getCornerImageHeight();
        double radius = Math
                .sqrt((double) (((centerX - x) * (centerX - x)) + (centerY - y)
                        * (centerY - y)));
        return radius < (3 * corner.getCornerImageWidth());
    }

    private boolean isNearCorners(float x, float y) {
        boolean near = false;
        for(Corner corner : corners) {
            if(isNearCorner(x, y, corner)) {
                near = true;
                break;
            }
        }
        return near;
    }

    private void drawRectangle() {
        int left, top, right, bottom;
        left = points[0].x;
        top = points[0].y;
        right = points[0].x;
        bottom = points[0].y;
        for(int i = 0; i < points.length; i++) {
            left = left > points[i].x ? points[i].x : left;
            top = top > points[i].y ? points[i].y : top;
            right = right < points[i].x ? points[i].x : right;
            bottom = bottom < points[i].y ? points[i].y : bottom;
        }

        Rectangle rectangle = new Rectangle(
                left + corners.get(0).getCornerImageWidth() / 2,
                top + corners.get(0).getCornerImageWidth() / 2,
                right + corners.get(2).getCornerImageWidth() / 2,
                bottom + corners.get(2).getCornerImageWidth() / 2
        );

        rectanglesHistory.add(rectangle);

        canvas.drawRect(
                rectangle.left,
                rectangle.top,
                rectangle.right,
                rectangle.bottom,
                paint
        );
    }

    private void initCorners() {
        for(Point point : points) {
            corners.add(new Corner(getContext(), R.drawable.corner, point));
        }
    }

    private void initPoints(float x, float y) {
        points[0] = new Point();
        points[0].x = (int) x;
        points[0].y = (int) y;

        points[1] = new Point();
        points[1].x = (int) x;
        points[1].y = (int) (y + 30);

        points[2] = new Point();
        points[2].x = (int) (x + 30);
        points[2].y = (int) (y + 30);

        points[3] = new Point();
        points[3].x = (int) (x + 30);
        points[3].y = (int) y;

        cornerId = 2;
        groupId = 1;
    }

    private class Rectangle {
        private float left;
        private float top;
        private float right;
        private float bottom;

        public Rectangle(float left, float top, float right, float bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }

    static class Corner {

        static int count;

        private Bitmap bitmap;
        private Point point;
        int id;

        public Corner(Context context, int resourceId, Point point) {
            this.bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
            this.point = point;
            this.id = count++;
        }

        public int getCornerImageWidth() {
            return bitmap.getWidth();
        }

        public int getCornerImageHeight() {
            return bitmap.getHeight();
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public int getX() {
            return point.x;
        }

        public int getY() {
            return point.y;
        }

        public int getID() {
            return id;
        }

        public void setX(int x) {
            point.x = x;
        }

        public void setY(int y) {
            point.y = y;
        }
    }

}
