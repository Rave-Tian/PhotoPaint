package com.example.photopaint.ui.components.paint;

import android.graphics.Matrix;
import android.view.MotionEvent;
import com.example.photopaint.messenger.AndroidUtilities;

import java.util.Vector;

public class Input {

    private RenderView renderView;

    private boolean beganDrawing;
    private boolean isFirst;
    private boolean hasMoved;
    private boolean clearBuffer;

    private Point lastLocation;
    private double lastRemainder;

    private Point[] points = new Point[3];
    private int pointsCount;

    private Matrix invertMatrix;
    private float[] tempPoint = new float[2];

    public Input(RenderView render) {
        renderView = render;
    }

    public void setMatrix(Matrix m) {
        invertMatrix = new Matrix();
        m.invert(invertMatrix);
    }

    public void process(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = renderView.getHeight() - event.getY();

        tempPoint[0] = x;
        tempPoint[1] = y;
        invertMatrix.mapPoints(tempPoint);

        Point location = new Point(tempPoint[0], tempPoint[1], 1.0f);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE: {
                if (!beganDrawing) {
                    // 如果当前的状态不是正在绘制中，那么开始绘制
                    beganDrawing = true;
                    hasMoved = false;
                    isFirst = true;

                    // 记录上一个点
                    lastLocation = location;

                    points[0] = location;
                    pointsCount = 1;

                    clearBuffer = true;
                } else {
                    //每次移动都是直线距离（无数个短的直线形成曲线），这里的distance是三维直线距离
                    float distance = location.getDistanceTo(lastLocation);
                    if (distance < AndroidUtilities.dp(5.0f)) {
                        // 移动距离小于5dp时，绘制状态不做变更
                        return;
                    }

                    if (!hasMoved) {
                        // 当发生移动之后回调onBeganDrawing
                        renderView.onBeganDrawing();
                        hasMoved = true;
                    }

                    // 把当前点记录下来
                    points[pointsCount] = location;
                    pointsCount++;

                    if (pointsCount == 3) {
                        //中间添加过渡的点让路径path更顺滑，并绘制这些点
                        smoothenAndPaintPoints(false);
                    }

                    lastLocation = location;
                }
            }
            break;

            case MotionEvent.ACTION_UP: {
                if (!hasMoved) {
                    if (renderView.shouldDraw()) {
                        location.edge = true;
                        paintPath(new Path(location));
                    }
                    reset();
                } else if (pointsCount > 0) {
                    smoothenAndPaintPoints(true);
                }

                pointsCount = 0;

                renderView.getPainting().commitStroke(renderView.getCurrentColor());
                beganDrawing = false;

                renderView.onFinishedDrawing(hasMoved);
            }
            break;
        }
    }

    private void reset() {
        pointsCount = 0;
    }

    private void smoothenAndPaintPoints(boolean ended) {
        if (pointsCount > 2) {
            Vector<Point> points = new Vector<>();

            Point prev2 = this.points[0];
            Point prev1 = this.points[1];
            Point cur = this.points[2];

            if (cur == null || prev1 == null || prev2 == null) {
                return;
            }

            prev2.mosaicColor = renderView.getMosaicColor((float) prev2.x, (float) prev2.y);
            prev1.mosaicColor = renderView.getMosaicColor((float) prev1.x, (float) prev1.y);
            cur.mosaicColor = renderView.getMosaicColor((float) cur.x, (float) cur.y);

//            Point midPoint1 = prev1.multiplySum(prev2, 0.5f);// 计算缓存的前两个点的中间点
//            midPoint1.mosaicColor = renderView.getMosaicColor((float) midPoint1.x, (float) midPoint1.y);
//            Point midPoint2 = cur.multiplySum(prev1, 0.5f);// 计算当前点和上一个点的中间点
//            midPoint2.mosaicColor = renderView.getMosaicColor((float) midPoint2.x, (float) midPoint2.y);
//
//            int segmentDistance = renderView.isMosaic() ? 30 : 1;//设置线段的距离为1px.
//            float distance = midPoint1.getDistanceTo(midPoint2);// 计算两个中间点的距离
//            int numberOfSegments = (int) Math.min(48, Math.max(Math.floor(distance / segmentDistance), 24));// 计算可以分成多少段
//
//            float t = 0.0f;
//            float step = 1.0f / (float) numberOfSegments;
//
//            for (int j = 0; j < numberOfSegments; j++) {
//                Point point = smoothPoint(midPoint1, midPoint2, prev1, t);// 添加过渡点
//                point.mosaicColor = renderView.getMosaicColor((float) point.x, (float) point.y);
//                if (isFirst) {// 是否为起始点，如果是起始点和结束点都需要标记
//                    point.edge = true;
//                    isFirst = false;
//                }
//                points.add(point);
//                t += step;
//            }
//
//            if (ended) {// 是否为结束点，当手势抬起的时候标记为结束点
//                midPoint2.edge = true;
//            }

            points.add(prev2);
            points.add(prev1);
            points.add(cur);

            Point[] result = new Point[points.size()];
            points.toArray(result);

            Path path = new Path(result);
            paintPath(path);

            System.arraycopy(this.points, 1, this.points, 0, 2);

            if (ended) {
                pointsCount = 0;
            } else {
                pointsCount = 2;
            }
        }
        else {
            Point[] result = new Point[pointsCount];
            System.arraycopy(this.points, 0, result, 0, pointsCount);
            Path path = new Path(result);
            paintPath(path);
        }
    }

    private Point smoothPoint(Point midPoint1, Point midPoint2, Point prev1, float t) {
        double a1 = Math.pow(1.0f - t, 2);
        double a2 = (2.0f * (1.0f - t) * t);
        double a3 = t * t;

        return new Point(midPoint1.x * a1 + prev1.x * a2 + midPoint2.x * a3, midPoint1.y * a1 + prev1.y * a2 + midPoint2.y * a3, 1.0f);
    }

    private void paintPath(final Path path) {
        path.setup(renderView.getCurrentColor(), renderView.getCurrentWeight(), renderView.getCurrentBrush());

        if (clearBuffer) {
            lastRemainder = 0.0f;
        }

        path.remainder = lastRemainder;// 接续上个path

        renderView.getPainting().paintStroke(path, clearBuffer, new Runnable() {
            @Override
            public void run() {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        lastRemainder = path.remainder;
                        clearBuffer = false;
                    }
                });
            }
        });
    }
}
