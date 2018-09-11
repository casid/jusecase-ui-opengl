package org.jusecase.ui.opengl.util;

public class ScreenConverter {
    private int windowWidth;
    private int windowHeight;
    private int nativeWidth;
    private int nativeHeight;

    public void setWindow(int w, int h) {
        windowWidth = w;
        windowHeight = h;
    }

    public void setNative(int w, int h) {
        nativeWidth = w;
        nativeHeight = h;
    }

    public int getNativeWidth() {
        return nativeWidth;
    }

    public int getNativeHeight() {
        return nativeHeight;
    }

    public double convertX(double x) {
        return (x * nativeWidth) / windowWidth;
    }

    public double convertY(double y) {
        return (y * nativeHeight) / windowHeight;
    }
}
