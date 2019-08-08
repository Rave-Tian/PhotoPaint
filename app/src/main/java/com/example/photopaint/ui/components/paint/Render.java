package com.example.photopaint.ui.components.paint;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Render {

    public static RectF RenderPath(Path path, RenderState state) {
        state.baseWeight = path.getBaseWeight();
        state.spacing = path.getBrush().getSpacing();
        state.alpha = path.getBrush().getAlpha();
        state.angle = path.getBrush().getAngle();
        state.scale = path.getBrush().getScale();
        state.red = 0.5f;
        state.green = 0.5f;
        state.blue = 0.5f;

        int length = path.getLength();
        if (length == 0) {
            return null;
        }

        if (length == 1) {
            // 如果是点就绘制Stamp
            state.red = (float) (Color.red(path.getPoints()[0].getMosaicColor())) /255;
            state.green = (float) (Color.green(path.getPoints()[0].getMosaicColor())) /255;
            state.blue = (float) (Color.blue(path.getPoints()[0].getMosaicColor())) /255;
            state.alpha = (float) (Color.alpha(path.getPoints()[0].getMosaicColor())) / 255;

            PaintStamp(path.getPoints()[0], state);
        } else {
            // 如果是线就绘制点与点之间的线段
            Point[] points = path.getPoints();
            state.prepare();

            for (int i = 0; i < points.length - 1; i++) {
                state.red = (float)(Color.red(path.getPoints()[i].getMosaicColor())) / 255;
                state.green = (float) (Color.green(path.getPoints()[i].getMosaicColor())) / 255;
                state.blue = (float) (Color.blue(path.getPoints()[i].getMosaicColor())) / 255;
                state.alpha = (float) (Color.alpha(path.getPoints()[1].getMosaicColor())) /255;
                PaintSegment(points[i], points[i + 1], state);
            }
        }

        path.remainder = state.remainder;

        return Draw(state);
    }

    private static void PaintSegment(Point lastPoint, Point point, RenderState state) {
        double distance = lastPoint.getDistanceTo(point);
        Point vector = point.substract(lastPoint);
        Point unitVector = new Point(1.0f, 1.0f, 0.0f);
        float vectorAngle = Math.abs(state.angle) > 0.0f ? state.angle : (float) Math.atan2(vector.y, vector.x);

        float brushWeight = state.baseWeight * state.scale;
        double step = Math.max(1.0f, state.spacing * brushWeight);

        if (distance > 0.0) {
            unitVector = vector.multiplyByScalar(1.0 / distance);
        }

        float boldenedAlpha = Math.min(1.0f, state.alpha * 1.15f);
        boolean boldenHead = lastPoint.edge;
        boolean boldenTail = point.edge;

        int count = (int) Math.ceil((distance - state.remainder) / step);
        int currentCount = state.getCount();
        state.appendValuesCount(count);
        state.setPosition(currentCount);

        Point start = lastPoint.add(unitVector.multiplyByScalar(state.remainder));

        boolean succeed = true;
        double f = state.remainder;
        for (; f <= distance; f += step) {
            float alpha = boldenHead ? boldenedAlpha : state.alpha;
            succeed = state.addPoint(start.toPointF(), brushWeight, vectorAngle, alpha, -1);
            if (!succeed) {
                break;
            }

            start = start.add(unitVector.multiplyByScalar(step));
            boldenHead = false;
        }

        if (succeed && boldenTail) {
            state.appendValuesCount(1);
            state.addPoint(point.toPointF(), brushWeight, vectorAngle, boldenedAlpha, -1);
        }

        state.remainder = f - distance;
    }

    private static void PaintStamp(Point point, RenderState state) {
        float brushWeight = state.baseWeight * state.scale;
        PointF start = point.toPointF();
        float angle = Math.abs(state.angle) > 0.0f ? state.angle : 0.0f;
        float alpha = state.alpha;

        state.prepare();
        state.appendValuesCount(1);
        state.addPoint(start, brushWeight, angle, alpha, 0);
    }

    private static RectF Draw(RenderState state) {
        RectF dataBounds = new RectF(0, 0, 0, 0);

        int count = state.getCount();
        if (count == 0) {
            return dataBounds;
        }

        int vertexDataSize = 8 * Float.SIZE / 8;
        int capacity = vertexDataSize * (count * 4 + (count - 1) * 2);
        ByteBuffer bb = ByteBuffer.allocateDirect(capacity);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer vertexData = bb.asFloatBuffer();
        vertexData.position(0);
        state.setPosition(0);

        int n = 0;
        for (int i = 0; i < count; i++) {
            float x = state.read();
            float y = state.read();
            float size = state.read();
            float angle = state.read();

            float alpha = state.read();
//            if(i % 10 == 0){
//                alpha = 1f;
//            }else {
//                alpha = 0.5f;
//            }
            float red = state.read();
//            if(i % 10 == 0){
//                red = 1f;
//            }else {
//                red = 0.5f;
//            }
            float green = state.read();
//            if(i % 10 == 0){
//                green = 1f;
//            }else {
//                green = 0.5f;
//            }
            float blue = state.read();
//            if(i % 10 == 0){
//                blue = 1f;
//            }else {
//                blue = 0.5f;
//            }

            RectF rect = new RectF(x - size, y - size, x + size, y + size);
            float[] points = new float[]{
                    rect.left, rect.top,
                    rect.right, rect.top,
                    rect.left, rect.bottom,
                    rect.right, rect.bottom
            };

            float centerX = rect.centerX();
            float centerY = rect.centerY();

            Matrix t = new Matrix();
            t.setRotate((float) Math.toDegrees(angle), centerX, centerY);
            t.mapPoints(points);
            t.mapRect(rect);

            Utils.RectFIntegral(rect);
            dataBounds.union(rect);

            if (n != 0) {
                vertexData.put(points[0]);
                vertexData.put(points[1]);
                vertexData.put(0);
                vertexData.put(0);
                vertexData.put(alpha);
                vertexData.put(red);
                vertexData.put(green);
                vertexData.put(blue);
                n++;
            }

            vertexData.put(points[0]);
            vertexData.put(points[1]);
            vertexData.put(0);
            vertexData.put(0);
            vertexData.put(alpha);
            vertexData.put(red);
            vertexData.put(green);
            vertexData.put(blue);
            n++;

            vertexData.put(points[2]);
            vertexData.put(points[3]);
            vertexData.put(1);
            vertexData.put(0);
            vertexData.put(alpha);
            vertexData.put(red);
            vertexData.put(green);
            vertexData.put(blue);
            n++;

            vertexData.put(points[4]);
            vertexData.put(points[5]);
            vertexData.put(0);
            vertexData.put(1);
            vertexData.put(alpha);
            vertexData.put(red);
            vertexData.put(green);
            vertexData.put(blue);
            n++;

            vertexData.put(points[6]);
            vertexData.put(points[7]);
            vertexData.put(1);
            vertexData.put(1);
            vertexData.put(alpha);
            vertexData.put(red);
            vertexData.put(green);
            vertexData.put(blue);
            n++;

            if (i != count - 1) {
                vertexData.put(points[6]);
                vertexData.put(points[7]);
                vertexData.put(1);
                vertexData.put(1);
                vertexData.put(alpha);
                vertexData.put(red);
                vertexData.put(green);
                vertexData.put(blue);
                n++;
            }
        }

        vertexData.position(0);
        FloatBuffer coordData = vertexData.slice();
        GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, vertexDataSize, coordData);
        GLES20.glEnableVertexAttribArray(0);

        vertexData.position(2);
        FloatBuffer texData = vertexData.slice();
        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, true, vertexDataSize, texData);
        GLES20.glEnableVertexAttribArray(1);

        vertexData.position(4);
        FloatBuffer alphaData = vertexData.slice();
        GLES20.glVertexAttribPointer(2, 1, GLES20.GL_FLOAT, true, vertexDataSize, alphaData);
        GLES20.glEnableVertexAttribArray(2);

        vertexData.position(5);
        FloatBuffer redData = vertexData.slice();
        GLES20.glVertexAttribPointer(3, 1, GLES20.GL_FLOAT, true, vertexDataSize, redData);
        GLES20.glEnableVertexAttribArray(3);

        vertexData.position(6);
        FloatBuffer greenData = vertexData.slice();
        GLES20.glVertexAttribPointer(4, 1, GLES20.GL_FLOAT, true, vertexDataSize, greenData);
        GLES20.glEnableVertexAttribArray(4);

        vertexData.position(7);
        FloatBuffer blueData = vertexData.slice();
        GLES20.glVertexAttribPointer(5, 1, GLES20.GL_FLOAT, true, vertexDataSize, blueData);
        GLES20.glEnableVertexAttribArray(5);


        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, n);

        return dataBounds;
    }
}
