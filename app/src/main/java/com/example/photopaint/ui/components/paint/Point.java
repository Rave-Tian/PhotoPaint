package com.example.photopaint.ui.components.paint;

import android.graphics.Color;
import android.graphics.PointF;

public class Point {

    public double x;
    public double y;
    public double z;

    public boolean edge;
    public int mosaicColor;

    public Point(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point(Point point) {
        x = point.x;
        y = point.y;
        z = point.z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)  {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Point)) {
            return false;
        }
        Point other = (Point) obj;
        return this.x == other.x && this.y == other.y && this.z == other.z;
    }

    Point multiplySum(Point point, double scalar) {
        return new Point((x + point.x) * scalar, (y + point.y) * scalar, (z + point.z) * scalar);
    }

    Point multiplyAndAdd(double scalar, Point point) {
        return new Point((x * scalar) + point.x, (y * scalar) + point.y, (z * scalar) + point.z);
    }

    private int getSmoothColor(Point point, double scalar){
        int r1 = Color.red(mosaicColor);
        int g1 = Color.green(mosaicColor);
        int b1 = Color.blue(mosaicColor);
        int a1 = Color.alpha(mosaicColor);

        int r2 = Color.red(point.getMosaicColor());
        int g2 = Color.green(point.getMosaicColor());
        int b2 = Color.blue(point.getMosaicColor());
        int a2 = Color.alpha(point.getMosaicColor());

        int r = (int) Math.min(255,(r1 + r2) * scalar);
        int g = (int) Math.min(255,(g1 + g2) * scalar);
        int b = (int) Math.min(255,(b1 + b2) * scalar);
        int a = (int) Math.min(255,(a1 + a2) * scalar);

        return  Color.argb(a, r, g, b);
    }

    private int addColor(int color1, int color2){
        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);
        int a1 = Color.alpha(color1);

        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);
        int a2 = Color.alpha(color2);

        int r = Math.min(255,(r1 + r2));
        int g = Math.min(255,(g1 + g2));
        int b = Math.min(255,(b1 + b2));
        int a = Math.min(255,(a1 + a2));

        return  Color.argb(a, r, g, b);
    }

    private int substractColor(int color1, int color2){
        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);
        int a1 = Color.alpha(color1);

        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);
        int a2 = Color.alpha(color2);

        int r = Math.max(0,(r1 - r2));
        int g = Math.max(0,(g1 - g2));
        int b = Math.max(0,(b1 - b2));
        int a = Math.max(0,(a1 - a2));

        return  Color.argb(a, r, g, b);
    }

    void alteringAddMultiplication(Point point, double scalar) {
        x = x + (point.x * scalar);
        y = y + (point.y * scalar);
        z = z + (point.z * scalar);
    }

    Point add(Point point) {
        return new Point(x + point.x, y + point.y, z + point.z);
    }

    Point substract(Point point) {
        return new Point(x - point.x, y - point.y, z - point.z);
    }

    Point multiplyByScalar(double scalar) {
        return new Point(x * scalar, y * scalar, z * scalar);
    }

    Point getNormalized() {
        return multiplyByScalar(1.0 / getMagnitude());
    }

    private double getMagnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    float getDistanceTo(Point point) {
        return (float) Math.sqrt(Math.pow(x - point.x, 2) + Math.pow(y - point.y, 2) + Math.pow(z - point.z, 2));
    }

    PointF toPointF() {
        return new PointF((float) x, (float) y);
    }

    public int getMosaicColor(){
        return this.mosaicColor;
    }
}

