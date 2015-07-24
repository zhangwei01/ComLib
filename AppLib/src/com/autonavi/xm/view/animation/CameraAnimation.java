
package com.autonavi.xm.view.animation;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * An animation that rotates the view on the Y axis between two specified
 * angles. This animation also adds a translation on the Z axis (depth) to
 * improve the effect.
 */
public class CameraAnimation extends Animation {

    public static final int ROTATE_AXIS_X = 0;

    public static final int ROTATE_AXIS_Y = 1;

    public static final int ROTATE_AXIS_Z = 2;

    private final float mFromDegrees;

    private final float mToDegrees;

    private float mCenterX;

    private float mCenterY;

    private int mCenterXType = RELATIVE_TO_SELF;

    private int mCenterYType = RELATIVE_TO_SELF;

    private final float mCenterXValue;

    private final float mCenterYValue;

    private final float mDepthZ;

    private int[] mRotateAxisList = new int[] {
        ROTATE_AXIS_Y
    };

    private final boolean mReverse;

    private Camera mCamera;

    public CameraAnimation(float fromDegrees, float toDegrees, float centerX, float centerY,
            float depthZ, boolean reverse) {
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        mCenterXValue = centerX;
        mCenterYValue = centerY;
        mDepthZ = depthZ;
        mReverse = reverse;
    }

    public CameraAnimation(float fromDegrees, float toDegrees, float centerX, float centerY,
            float depthZ, int[] rotateAxisList, boolean reverse) {
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        mCenterXValue = centerX;
        mCenterYValue = centerY;
        mDepthZ = depthZ;
        mRotateAxisList = rotateAxisList;
        mReverse = reverse;
    }

    public CameraAnimation(float fromDegrees, float toDegrees, int centerXType, float centerX,
            int centerYType, float centerY, float depthZ, int[] rotateAxisList, boolean reverse) {
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        mCenterXType = centerXType;
        mCenterYType = centerYType;
        mCenterXValue = centerX;
        mCenterYValue = centerY;
        mDepthZ = depthZ;
        mRotateAxisList = rotateAxisList;
        mReverse = reverse;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mCamera = new Camera();
        mCenterX = resolveSize(mCenterXType, mCenterXValue, width, parentWidth);
        mCenterY = resolveSize(mCenterYType, mCenterYValue, height, parentHeight);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float fromDegrees = mFromDegrees;
        float degrees = fromDegrees + (mToDegrees - fromDegrees) * interpolatedTime;

        float centerX = mCenterX;
        float centerY = mCenterY;
        Camera camera = mCamera;

        int[] rotateAxisList = mRotateAxisList;

        Matrix matrix = t.getMatrix();

        camera.save();
        if (mReverse) {
            camera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime);
        } else {
            camera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime));
        }
        for (int axis : rotateAxisList) {
            switch (axis) {
                case ROTATE_AXIS_X:
                    camera.rotateX(degrees);
                    break;
                case ROTATE_AXIS_Y:
                    camera.rotateY(degrees);
                    break;
                case ROTATE_AXIS_Z:
                    camera.rotateZ(degrees);
                    break;
            }
        }

        camera.getMatrix(matrix);
        camera.restore();

        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
    }
}
