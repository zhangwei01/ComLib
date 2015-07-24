
package com.autonavi.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES10;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public class GLBitmapSprite {

    private static final int SHORT_SIZE_BYTES = Short.SIZE / Byte.SIZE;

    private static final int INTEGER_SIZE_BYTES = Integer.SIZE / Byte.SIZE;

    private Bitmap mBitmap;

    private int mTextureId = -1;

    private int mWidth, mHeight;

    private int mPow2Width, mPow2Height;

    private final ShortBuffer mVerticleBuffer;

    private final ShortBuffer mCoordBuffer;

    private final ShortBuffer mIndexBuffer;

    private final boolean mClearUseless;

    private ByteBuffer mPow2PixelsBuffer;

    public GLBitmapSprite() {
        this(null, false);
    }

    public GLBitmapSprite(Bitmap bitmap) {
        this(bitmap, false);
    }

    public GLBitmapSprite(Bitmap bitmap, boolean clearUseless) {
        mVerticleBuffer = ByteBuffer.allocateDirect(4 * 2 * SHORT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        mCoordBuffer = ByteBuffer.allocateDirect(4 * 2 * SHORT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        mCoordBuffer.put(new short[] {
                // 0,0--1,0
                // | |
                // 0,1--1,1
                0, 1,/*;*/1, 1,/*;*/0, 0,/*;*/1, 0
        }).position(0);

        mIndexBuffer = ByteBuffer.allocateDirect(6 * SHORT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        mIndexBuffer.put(new short[] {
                0, 1, 2, 1, 2, 3
        }).position(0);
        mClearUseless = clearUseless;
        setBitmap(bitmap);
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }
        mBitmap = bitmap;
        setSize(bitmap.getWidth(), bitmap.getHeight());
    }

    private void setSize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        mWidth = width;
        mHeight = height;
        int p2width = mPow2Width = roundUpPower2(width);
        int p2height = mPow2Height = roundUpPower2(height);

        int dh = height - p2height;
        mVerticleBuffer.put(new short[] {
                0, (short) dh,//
                (short) p2width, (short) dh,//
                0, (short) (p2height + dh), //
                (short) p2width, (short) (p2height + dh)
        }).position(0);

        if (mClearUseless) {
            if (width != p2width || height != p2height) {
                mPow2PixelsBuffer = ByteBuffer.allocateDirect(
                        INTEGER_SIZE_BYTES * p2width * p2height).order(ByteOrder.nativeOrder());
            }
        }
    }

    public void bindTexture() {
        int[] textures = new int[1];
        GLES10.glGenTextures(1, textures, 0);
        mTextureId = textures[0];

        GLES10.glBindTexture(GL10.GL_TEXTURE_2D, mTextureId);
        GLES10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
    }

    public void initTexImage2D() {
        GLES10.glBindTexture(GL10.GL_TEXTURE_2D, mTextureId);
        if (mWidth == mPow2Width && mHeight == mPow2Height) {
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmap, 0);
        } else {
            GLES10.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, mPow2Width, mPow2Height, 0,
                    GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, mPow2PixelsBuffer);
            GLUtils.texSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0, mBitmap);
        }
    }

    public void texImage2D() {
        GLES10.glBindTexture(GL10.GL_TEXTURE_2D, mTextureId);
        if (mWidth == mPow2Width && mHeight == mPow2Height) {
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmap, 0);
        } else {
            GLUtils.texSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0, mBitmap);
        }
    }

    public void draw() {
        GLES10.glBindTexture(GL10.GL_TEXTURE_2D, mTextureId);
        GLES10.glVertexPointer(2, GL10.GL_SHORT, 0, mVerticleBuffer);
        GLES10.glTexCoordPointer(2, GL10.GL_SHORT, 0, mCoordBuffer);
        GLES10.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
    }

    public void drawAlignCenter(int x, int y) {
        GLES10.glPushMatrix();
        GLES10.glTranslatef(x - (mWidth >> 1), y - (mHeight >> 1), 0);
        draw();
        GLES10.glPopMatrix();
    }

    public static void beginDrawing() {
        GLES10.glEnable(GL10.GL_TEXTURE_2D);
        GLES10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        GLES10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

    public static void endDrawing() {
        GLES10.glDisable(GL10.GL_TEXTURE_2D);
        GLES10.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        GLES10.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

    /**
     * Find the smallest power of two >= the input value. (Doesn't work for
     * negative numbers.)
     * 
     * @param value input value
     * @return the smallest power of two
     */
    public static int roundUpPower2(int value) {
        value = value - 1;
        value = value | (value >> 1);
        value = value | (value >> 2);
        value = value | (value >> 4);
        value = value | (value >> 8);
        value = value | (value >> 16);
        return value + 1;
    }
}
