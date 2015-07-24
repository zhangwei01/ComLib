
package com.autonavi.xm.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.autonavi.xm.app.lib.R;

public class CompoundScrollView extends LinearLayout {

    private ScrollView mScrollView;

    private CompoundScrollbar mScrollbar;

    public CompoundScrollView(Context context) {
        this(context, null);
    }

    public CompoundScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.compound_scroll_view, this);

        setOrientation(HORIZONTAL);

        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
        mScrollbar = (CompoundScrollbar) findViewById(R.id.scrollbar);
        mScrollbar.setScrollView(mScrollView);
    }

    public ScrollView getScrollView() {
        return mScrollView;
    }

    public void setPageNumberEnabled(boolean enabled) {
        mScrollbar.setPageNumberEnabled(enabled);
    }

    @Override
    public void addView(View child) {
        if (mScrollView == null) {
            super.addView(child);
        } else {
            mScrollView.addView(child);
        }
    }

    @Override
    public void addView(View child, int index) {
        if (mScrollView == null) {
            super.addView(child, index);
        } else {
            mScrollView.addView(child, index);
        }
    }

    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        if (mScrollView == null) {
            super.addView(child, params);
        } else {
            mScrollView.addView(child, params);
        }
    }

    @Override
    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        if (mScrollView == null) {
            super.addView(child, index, params);
        } else {
            mScrollView.addView(child, index, params);
        }
    }

    @Override
    public void addView(View child, int width, int height) {
        if (mScrollView == null) {
            super.addView(child, width, height);
        } else {
            mScrollView.addView(child, width, height);
        }
    }

}
