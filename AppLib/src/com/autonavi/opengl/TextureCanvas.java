
package com.autonavi.opengl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;

public class TextureCanvas extends Canvas {

    private final GLBitmapSprite mGlBitmapSprite;

    public TextureCanvas() {
        this(0, 0);
    }

    public TextureCanvas(int width, int height) {
        super();
        mGlBitmapSprite = new GLBitmapSprite();
        setSize(width, height);
    }

    public void setSize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        Bitmap bitmap = mGlBitmapSprite.getBitmap();
        if (bitmap != null && bitmap.getWidth() == width && bitmap.getHeight() == height) {
            return;
        }
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mGlBitmapSprite.setBitmap(bitmap);
        setBitmap(bitmap);
    }

    public void clearColor() {
        drawColor(0x0, PorterDuff.Mode.CLEAR);
    }

    public void bindTexture() {
        mGlBitmapSprite.bindTexture();
    }

    public void initTexImage2D() {
        mGlBitmapSprite.initTexImage2D();
    }

    public void texImage2D() {
        mGlBitmapSprite.texImage2D();
    }

    public void draw() {
        mGlBitmapSprite.draw();
    }

    public void drawAlignCenter(int x, int y) {
        mGlBitmapSprite.drawAlignCenter(x, y);
    }

}
