
package com.autonavi.xm.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class BaseView extends android.view.View {

    private OnSizeChangeListener mOnSizeChangedListener;

    public BaseView(Context context) {
        this(context, null, 0);
    }

    public BaseView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnSizeChangeListener(OnSizeChangeListener listener) {
        mOnSizeChangedListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mOnSizeChangedListener != null) {
            mOnSizeChangedListener.onSizeChanged(this, w, h, oldw, oldh);
        }
    }

    /**
     * 设置View的enabled状态，可以防止disenabled改变为enabled时，pressed状态未解除的问题
     * 
     * @param view 要设置的View
     * @param enabled 是否enabled
     */
    public static void setViewEnabled(View view, boolean enabled) {
        if (!view.isEnabled()) {
            // 按钮状态强制为非pressed，防止禁用时状态未及时切换的bug
            view.setPressed(false);
        }
        view.setEnabled(enabled);
    }

    /**
     * 设置ViewGroup所有子View的enabled状态，递归，包括自身
     * 
     * @param group 要设置的ViewGroup
     * @param enabled 是否enabled
     */
    public static void setViewGroupEnabled(ViewGroup group, boolean enabled) {
        if (group == null) {
            return;
        }
        for (int i = group.getChildCount() - 1; i >= 0; i--) {
            View child = group.getChildAt(i);
            if (child instanceof ViewGroup) {
                setViewGroupEnabled((ViewGroup) child, enabled);
            } else {
                child.setEnabled(enabled);
            }
        }
        group.setEnabled(enabled);
    }

}
