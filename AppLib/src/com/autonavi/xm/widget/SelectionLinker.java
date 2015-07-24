
package com.autonavi.xm.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.autonavi.xm.app.lib.R;
import com.autonavi.xm.widget.CircularListView.OnFocusChangeListener;

public class SelectionLinker extends FrameLayout implements OnItemSelectedListener,
        OnFocusChangeListener {

    private LinkedListView mLinkedView;

    private boolean mIsItemSelected = false;

    private int mXHall;

    private int mYHall;

    private Point mLinkedPoint;

    private int mHorLineLen = 20;

    private Point mCenterPoint;

    private int mRadius;

    private Drawable mDrawableLine;

    private Drawable mDrawableLineBelow;

    private Drawable mDrawableStartPoint;

    private Drawable mDrawableItemSelector;

    private int mRadiusStartPoint;

    private Point mCornerPoint;

    private Point mStartPoint;

    private double mAngle;

    private int mDrawableLineHeight;

    private int mSlantLen;

    private int mExtraPadding;

    private Bitmap mBgBitmap;

    private final Rect mAreaListSelector = new Rect();

    private ImageView mImgView;

    private View mSelectedView;

    public SelectionLinker(Context context) {
        super(context);
        init();
    }

    public SelectionLinker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SelectionLinker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public int getIntrinsicHeight() {
        return mImgView.getHeight();
    }

    public int getIntrinsicWidth() {
        return mImgView.getWidth();
    }

    private void init() {
        mImgView = new ImageView(getContext()) {
            @Override
            protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                super.onSizeChanged(w, h, oldw, oldh);
                createBackgroundBitmap();
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (event.getX() > 0 && event.getY() > 0 && event.getX() < mBgBitmap.getWidth()
                        && event.getY() < mBgBitmap.getHeight()
                        && !(mBgBitmap.getPixel((int) event.getX(), (int) event.getY()) == 0)) {
                    return true;
                }

                return super.onTouchEvent(event);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);

                if (!mIsItemSelected || !mLinkedView.isFocused()) {
                    return;
                }

                if (mCornerPoint != null && mStartPoint != null) {
                    //设置抗锯齿
                    canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                            | Paint.FILTER_BITMAP_FLAG));

                    if (mDrawableLine != null) {
                        drawSlant(canvas);
                        Drawable line = mAngle > 0 ? mDrawableLine : mDrawableLineBelow;
                        line.setBounds(mCornerPoint.x - 1, mCornerPoint.y - mDrawableLineHeight / 2
                                - 1, mLinkedPoint.x, mLinkedPoint.y + mDrawableLineHeight / 2 + 2);
                        line.draw(canvas);
                    }
                    if (mDrawableItemSelector != null) {
                        mDrawableItemSelector.setBounds(mAreaListSelector);
                        if (mAreaListSelector.top < mYHall) {
                            canvas.clipRect(new Rect(mAreaListSelector.left, 0,
                                    mAreaListSelector.right, mYHall), Region.Op.XOR);
                        }
                        mDrawableItemSelector.draw(canvas);
                    }
                    if (mDrawableStartPoint != null) {
                        mDrawableStartPoint.setBounds(mStartPoint.x - mRadiusStartPoint,
                                mStartPoint.y - mRadiusStartPoint, mStartPoint.x
                                        + mRadiusStartPoint, mStartPoint.y + mRadiusStartPoint);
                        mDrawableStartPoint.draw(canvas);
                    }
                }
            }

            /**
             * 画斜线
             * 
             * @param canvas
             */
            private void drawSlant(Canvas canvas) {
                canvas.save();
                if (mAngle > 0) {
                    canvas.clipRect(new Rect(mCornerPoint.x - mDrawableLineHeight, mCornerPoint.y
                            - 3 * mDrawableLineHeight / 2,
                            mCornerPoint.x + 2 * mDrawableLineHeight, mCornerPoint.y
                                    - mDrawableLineHeight / 2), Region.Op.XOR);
                } else if (mAngle < 0) {
                    canvas.clipRect(new Rect(mCornerPoint.x - mDrawableLineHeight, mCornerPoint.y
                            + mDrawableLineHeight / 2 - 1,
                            mCornerPoint.x + 2 * mDrawableLineHeight, mCornerPoint.y
                                    + mDrawableLineHeight), Region.Op.XOR);
                }
                canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                        | Paint.FILTER_BITMAP_FLAG));
                canvas.translate(mStartPoint.x, mStartPoint.y);
                canvas.rotate(-(float) (mAngle * 180 / Math.PI));
                mDrawableLine.setFilterBitmap(true);
                Drawable line = mAngle > 0 ? mDrawableLine : mDrawableLineBelow;
                line.setBounds(0, -mDrawableLineHeight / 2 - 1,
                        mSlantLen + mDrawableLineHeight - 6, mDrawableLineHeight / 2 + 2);
                line.draw(canvas);
                canvas.restore();
            }
        };

        addView(mImgView);
        LayoutParams lp = new LayoutParams((LayoutParams) mImgView.getLayoutParams());
        lp.width = LayoutParams.WRAP_CONTENT;
        lp.height = LayoutParams.MATCH_PARENT;
        mImgView.setLayoutParams(lp);
        mImgView.setBackgroundResource(R.drawable.bg_selection_linker);
        mImgView.setScaleType(ScaleType.CENTER_INSIDE);
        setLine(R.drawable.line);
        setStartPoint(R.drawable.start_point);
        setItemSelector(R.drawable.item_selector);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setClipChildren(false);
    }

    private void createBackgroundBitmap() {
        recycleBackgroundBitmap();
        if (mImgView.getWidth() == 0 || mImgView.getHeight() == 0) {
            return;
        }
        if (mBgBitmap == null) {
            mBgBitmap = Bitmap.createBitmap(mImgView.getWidth(), mImgView.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mBgBitmap);
            mImgView.getBackground().setBounds(0, 0, getWidth(), getHeight());
            mImgView.getBackground().draw(canvas);
        }
    }

    private void recycleBackgroundBitmap() {
        if (mBgBitmap != null) {
            mBgBitmap.recycle();
            mBgBitmap = null;
        }
    }

    public void setItemSelector(int resId) {
        mDrawableItemSelector = getResources().getDrawable(resId);
    }

    public void setLine(int resId) {
        mDrawableLine = getResources().getDrawable(resId);
        if (mDrawableLine != null) {
            mDrawableLineHeight = mDrawableLine.getIntrinsicHeight();
        }
        mDrawableLineBelow = getResources().getDrawable(R.drawable.line_below);
    }

    public void link(LinkedListView view) {
        mLinkedView = view;
        mLinkedView.setOnItemSelectedListener(this);
        mLinkedView.setOnFocusChangeListener(this);
        mLinkedView.setSelectionLinker(this);
        createBackgroundBitmap();
    }

    private void setAdjustion(int xHall, int yHall) {
        mXHall = xHall;
        mYHall = yHall;
    }

    public void setHorizontalLineLength(int lenght) {
        mHorLineLen = lenght;
    }

    public void setStartPoint(int resId) {
        mDrawableStartPoint = getResources().getDrawable(resId);
        mRadiusStartPoint = mDrawableStartPoint != null ? mDrawableStartPoint.getIntrinsicHeight() / 2
                : 0;
    }

    public void setSelectionDrawable(int resId) {
        mImgView.setImageResource(resId);
    }

    private void setVariableParam() {
        if (mSelectedView != null) {
            if (mSelectedView.getTag() != null && mSelectedView.getTag() instanceof Integer
                    && mIsItemSelected) {
                mImgView.setImageResource(((Integer) mSelectedView.getTag()).intValue());
            }

            mLinkedPoint = new Point(mSelectedView.getLeft() + mSelectedView.getPaddingLeft()
                    + mXHall - mLinkedView.getSelectionPaddingLeft(),
                    (mSelectedView.getTop() + mSelectedView.getBottom()) / 2 + mYHall);

            mCornerPoint = new Point(mLinkedPoint.x - mHorLineLen, mLinkedPoint.y);
            mAngle = Math.atan((double) (mCenterPoint.y - mCornerPoint.y)
                    / (mCornerPoint.x - mCenterPoint.x));
            mStartPoint = new Point(mCenterPoint.x
                    + (int) ((mRadius + mRadiusStartPoint) * Math.cos(mAngle)), mCenterPoint.y
                    - (int) ((mRadius + mRadiusStartPoint) * Math.sin(mAngle)));
            mSlantLen = (Math.abs(mStartPoint.y - mCornerPoint.y) <= 2) ? (mCornerPoint.x - mStartPoint.x)
                    : (int) ((mStartPoint.y - mCornerPoint.y) / Math.sin(mAngle));

            mAreaListSelector.set(mLinkedPoint.x + mXHall, mSelectedView.getTop() + mYHall,
                    mSelectedView.getRight() - mLinkedView.getVerticalScrollbarWidth()
                            - mSelectedView.getPaddingRight() + mXHall, mSelectedView.getBottom()
                            + mYHall);
        }
    }

    private void setConstParam() {
        Rect backgroundPadding = new Rect();
        mImgView.getBackground().getPadding(backgroundPadding);
        // 背景图上下被拉伸长度
        mExtraPadding = (mImgView.getHeight() - mImgView.getBackground().getIntrinsicHeight()) / 2;
        backgroundPadding = new Rect((backgroundPadding.left), backgroundPadding.top
                + mExtraPadding, (backgroundPadding.right), backgroundPadding.bottom
                + mExtraPadding);
        mRadius = (mImgView.getWidth() - backgroundPadding.left - backgroundPadding.right) / 2;
        mCenterPoint = new Point(backgroundPadding.left + mRadius, backgroundPadding.top + mRadius);
        int[] linkedViewLoc = new int[2];
        mLinkedView.getLocationOnScreen(linkedViewLoc);
        int[] linkerLoc = new int[2];
        mImgView.getLocationOnScreen(linkerLoc);
        // 设置坐标系差值
        setAdjustion(linkedViewLoc[0] - linkerLoc[0], linkedViewLoc[1] - linkerLoc[1]);
    }

    private void invalidateDrawingArea() {
        invalidate(getLeft(), getTop(), mAreaListSelector.right, getBottom());
        if (mImgView != null) {
            mImgView.invalidate();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (!mHasInit) {
            setConstParam();
            mHasInit = true;
        }
        mIsItemSelected = true;
        mSelectedView = view;
        setVariableParam();
        invalidateDrawingArea();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        mHasInit = false;
        mIsItemSelected = false;
        invalidateDrawingArea();
    }

    @Override
    protected void onDetachedFromWindow() {
        recycleBackgroundBitmap();
        mLinkedView = null;
        super.onDetachedFromWindow();
    }

    private boolean mHasInit = false;

    @Override
    public void onFocusChange(boolean focused, int direction) {
        if (!focused) {
            mIsItemSelected = false;
            invalidateDrawingArea();
        } else if (mHasInit) {
            if (direction == 0) {
                mIsItemSelected = true;
                setVariableParam();
                invalidateDrawingArea();
            }
        }
    }
}
