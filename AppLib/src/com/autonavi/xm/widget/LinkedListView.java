
package com.autonavi.xm.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class LinkedListView extends CircularListView {

    private SelectionLinker mLinker;

    private int mSelectionMarginLeft = 0;

    private int mSelectionMarginRight = 20;

    private int mSelectionPaddingLeft = 0;

    private int mSelectionPaddingRight = 0;

    private int mPaddingLeft = 0;

    private int mPaddingRight = 0;

    private boolean mHasInit = false;

    public LinkedListView(Context context) {
        super(context);
    }

    public LinkedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LinkedListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public int getSelectionPaddingLeft() {
        return mSelectionPaddingLeft;
    }

    public int getSelectionPaddingRight() {
        return mSelectionPaddingRight;
    }

    public void setSelectionMarginLeft(int leftMargin) {
        mSelectionMarginLeft = leftMargin;
    }

    public void setSelectionMarginRight(int rightMargin) {
        mSelectionMarginRight = rightMargin;
    }

    void setSelectionLinker(SelectionLinker linker) {
        mLinker = linker;
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params) {
        resetChildPadding(child);
        return super.addViewInLayout(child, index, params);
    }

    private void resetChildPadding(View child) {
        if (!mHasInit) {
            mSelectionPaddingLeft = child.getPaddingLeft();
            mSelectionPaddingRight = child.getPaddingRight();
            mPaddingLeft = mLinker.getIntrinsicWidth() + mSelectionPaddingLeft
                    + mSelectionMarginLeft;
            mPaddingRight = mSelectionPaddingRight + mSelectionMarginRight;
            mHasInit = true;
        }
        child.setPadding(mPaddingLeft, child.getPaddingTop(), mPaddingRight,
                child.getPaddingBottom());
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params,
            boolean preventRequestLayout) {
        resetChildPadding(child);
        return super.addViewInLayout(child, index, params, preventRequestLayout);
    }

}
