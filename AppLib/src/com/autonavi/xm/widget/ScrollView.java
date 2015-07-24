
package com.autonavi.xm.widget;

import android.content.Context;
import android.util.AttributeSet;

public class ScrollView extends android.widget.ScrollView {

    private OnScrollChangedListener mOnScrollChangedListener;

    public ScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollView(Context context) {
        super(context);
    }

    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        mOnScrollChangedListener = listener;
    }

    public int getContentHeight() {
        return getChildAt(0).getHeight();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangedListener != null) {
            mOnScrollChangedListener.onScrollChanged(this, l, t, oldl, oldt);
        }
    }

    public static interface OnScrollChangedListener {

        public void onScrollChanged(ScrollView scrollView, int l, int t, int oldl, int oldt);

    }

}
