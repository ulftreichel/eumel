package com.eumel.level.design;

public class PlatformInfo {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final boolean isBreakable;

    public PlatformInfo(double x, double y, double width, double height, boolean isBreakable) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isBreakable = isBreakable;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public boolean isBreakable() {
        return isBreakable;
    }
}