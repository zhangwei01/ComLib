
package com.autonavi.xm.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import com.autonavi.xm.app.lib.R;

public class SimpleIndicator extends View implements Indicator {

    private Drawable mNormalDrawable;

    private Drawable mCurrentDrawable;

    private int mTotal;

    private int mCurrent;

    private int mSpacing = 10;

    private boolean mGoneWhenEmpty = true;

    public SimpleIndicator(Context context) {
        this(context, null, 0);
    }

    public SimpleIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setNormalResource(R.drawable.simple_indicator_normal);
        setCurrentResource(R.drawable.simple_indicator_current);
    }

    @Override
    public void setTotal(int total) {
        mTotal = total;
        mCurrent = Math.max(0, Math.min(total - 1, mCurrent));
        if (total <= 1 && mGoneWhenEmpty) {
            if (getVisibility() == VISIBLE) {
                setVisibility(GONE);
            }
        } else {
            if (getVisibility() != VISIBLE) {
                setVisibility(VISIBLE);
            }
        }
        requestLayout();
    }

    @Override
    public int getTotal() {
        return mTotal;
    }

    @Override
    public void setCurrent(int current) {
        current = Math.max(0, Math.min(mTotal - 1, current));
        mCurrent = current;
        invalidate();
    }

    @Override
    public int getCurrent() {
        return mCurrent;
    }

    public void setNormalResource(int normal) {
        mNormalDrawable = getResources().getDrawable(normal);
        invalidate();
    }

    public void setNormalDrawable(Drawable normal) {
        mNormalDrawable = normal;
        invalidate();
    }

    public Drawable getNormalDrawable() {
        return mNormalDrawable;
    }

    public void setCurrentResource(int current) {
        mCurrentDrawable = getResources().getDrawable(current);
        invalidate();
    }

    public void setCurrentDrawable(Drawable Current) {
        mCurrentDrawable = Current;
        invalidate();
    }

    public Drawable getCurrentDrawable() {
        return mCurrentDrawable;
    }

    public void setSpacing(int spacing) {
        if (spacing < 0) {
            return;
        }
        mSpacing = spacing;
        invalidate();
    }

    public int getSpacing() {
        return mSpacing;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable normalDrawable = mNormalDrawable;
        Drawable currentDrawable = mCurrentDrawable;
        int width = (normalDrawable.getMinimumWidth() + mSpacing) * (mTotal - 1)
                + currentDrawable.getMinimumWidth();
        int height = Math
                .max(normalDrawable.getMinimumHeight(), currentDrawable.getMinimumHeight());
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int halfHeight = getHeight() / 2;
        Drawable normalDrawable = mNormalDrawable;
        int spacing = mSpacing;
        int current = mCurrent;
        int total = mTotal;
        int widthSum = 0;
        Drawable drawable = null;
        int minWidth = 0;
        int minHeight = 0;
        for (int i = 0; i < total; i++) {
            if (i == current) {
                drawable = mCurrentDrawable;
            } else {
                drawable = normalDrawable;
            }
            if (drawable == null) {
                continue;
            }
            minWidth = drawable.getMinimumWidth();
            minHeight = drawable.getMinimumHeight();
            drawable.setBounds(widthSum, halfHeight - minHeight / 2, widthSum + minWidth,
                    halfHeight + minHeight / 2);
            drawable.draw(canvas);
            widthSum += minWidth + spacing;
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.total = mTotal;
        state.current = mCurrent;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        if (savedState.total != -1) {
            mTotal = savedState.total;
        }
        if (savedState.current != -1) {
            mCurrent = savedState.current;
        }
    }

    public static class SavedState extends BaseSavedState {

        int total = -1;

        int current = -1;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            total = in.readInt();
            current = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(total);
            out.writeInt(current);
        }

        public static Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
