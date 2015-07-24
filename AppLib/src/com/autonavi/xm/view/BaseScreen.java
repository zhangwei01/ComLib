
package com.autonavi.xm.view;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.autonavi.xm.app.lib.R;
import com.autonavi.xm.content.SharedIntent;

public class BaseScreen {

    protected Activity mActivity;

    private Resources mResources;

    private LayoutInflater mLayoutInflater;

    private boolean mIsNoTitle = false;

    private boolean mIsTitleSimple = false;

    private CharSequence mTitle;

    private View mScreenView;

    private View mTitleBar;

    private TextView mTitleText;

    private FrameLayout mTitleContentFrame;

    private FrameLayout mTitleRightFrame;

    private FrameLayout mTitleLeftFrame;

    private FrameLayout mContentFrame;

    public BaseScreen(Context context) {
        if (!(context instanceof Activity)) {
            throw new IllegalArgumentException("The context must be an \"Activity\"!");
        }
        mActivity = (Activity) context;
        mResources = mActivity.getResources();
        mLayoutInflater = LayoutInflater.from(context);

        mScreenView = mLayoutInflater.inflate(R.layout.base_screen_view, null);
        mContentFrame = (FrameLayout) mScreenView.findViewById(R.id.content);
    }

    public View getScreenView() {
        return mScreenView;
    }

    public void setNoTitle(boolean notitle) {
        mIsNoTitle = notitle;
        View titleBar = mScreenView.findViewById(R.id.title_bar);
        if (titleBar != null) {
            if (notitle && titleBar.getVisibility() == VISIBLE) {
                titleBar.setVisibility(GONE);
            } else if (!notitle && titleBar.getVisibility() != VISIBLE) {
                titleBar.setVisibility(VISIBLE);
            }
        } else if (!notitle) {
            titleBar = mTitleBar = ((ViewStub) mScreenView.findViewById(R.id.stub_title_bar))
                    .inflate();
            mTitleText = (TextView) titleBar.findViewById(R.id.title);
            setTitle(mTitle != null ? mTitle : mActivity.getTitle());

            mTitleRightFrame = (FrameLayout) mScreenView.findViewById(R.id.extra_frame_right);
            mTitleLeftFrame = (FrameLayout) mScreenView.findViewById(R.id.extra_frame_left);
            mTitleContentFrame = (FrameLayout) mScreenView.findViewById(R.id.title_content);
        }
    }

    public boolean isNoTitle() {
        return mIsNoTitle;
    }

    public void setTitle(CharSequence title) {
        mTitle = title;
        if (mTitleText != null) {
            mTitleText.setText(title);
        }
    }

    public void setTitle(int titleId) {
        setTitle(mResources.getText(titleId));
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public void setTitleIcon(int resId) {
        if (mTitleText != null) {
            mTitleText.setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0);
        }
    }

    public void setTitleContent(int layoutResId) {
        checkTitleBarValid();
        setTitleContent(layoutResId > 0 ? mLayoutInflater.inflate(layoutResId, mTitleContentFrame,
                false) : null);
    }

    public void setTitleContent(View view) {
        setTitleContent(view, view != null ? view.getLayoutParams() : null);
    }

    public void setTitleContent(View view, ViewGroup.LayoutParams params) {
        checkTitleBarValid();
        mTitleContentFrame.removeAllViews();
        if (view != null) {
            mTitleContentFrame.addView(view, getDefaultLayoutParams(params));
        }
    }

    public void setTitleRightContent(int layoutResId) {
        checkTitleBarValid();
        setTitleRightContent(layoutResId > 0 ? mLayoutInflater.inflate(layoutResId,
                mTitleRightFrame, false) : null);
    }

    public void setTitleRightContent(View view) {
        setTitleRightContent(view, view != null ? view.getLayoutParams() : null);
    }

    public void setTitleRightContent(View view, ViewGroup.LayoutParams params) {
        checkTitleBarValid();
        mTitleRightFrame.removeAllViews();
        if (view != null) {
            mTitleRightFrame.addView(view, getDefaultLayoutParams(params));
        }
    }

    public void setTitleLeftContent(int layoutResId) {
        setTitleLeftContent(layoutResId > 0 ? mLayoutInflater.inflate(layoutResId, mTitleLeftFrame,
                false) : null);
    }

    public void setTitleLeftContent(View view) {
        setTitleLeftContent(view, view != null ? view.getLayoutParams() : null);
    }

    public void setTitleLeftContent(View view, ViewGroup.LayoutParams params) {
        checkTitleBarValid();
        mTitleLeftFrame.removeAllViews();
        if (view != null) {
            mTitleLeftFrame.addView(view, getDefaultLayoutParams(params));
        }
    }

    public void setContentView(int layoutResId) {
        mLayoutInflater.inflate(layoutResId, mContentFrame, true);
        initTitleBar();
    }

    public void setContentView(View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        setContentView(view, layoutParams);
    }

    public void setContentView(View view, ViewGroup.LayoutParams params) {
        if (view == null) {
            return;
        }
        mContentFrame.removeAllViews();
        mContentFrame.addView(view, getDefaultLayoutParams(params));
        initTitleBar();
    }

    //for compatible
    @SuppressWarnings("deprecation")
    public void setBackground(Drawable background) {
        mScreenView.setBackgroundDrawable(background);
    }

    public void setBackground(int resId) {
        mScreenView.setBackgroundResource(resId);
    }

    public void setBackgroundColor(int color) {
        mScreenView.setBackgroundColor(color);
    }

    //for compatible
    @SuppressWarnings("deprecation")
    public void setTitleBackground(Drawable background) {
        checkTitleBarValid();
        mTitleBar.setBackgroundDrawable(background);
    }

    public void setTitleBackground(int resId) {
        checkTitleBarValid();
        mTitleBar.setBackgroundResource(resId);
    }

    public void setTitleBackgroundColor(int color) {
        checkTitleBarValid();
        mTitleBar.setBackgroundColor(color);
    }

    //for compatible
    @SuppressWarnings("deprecation")
    public void setContentBackground(Drawable background) {
        mContentFrame.setBackgroundDrawable(background);
    }

    public void setContentBackground(int resId) {
        mContentFrame.setBackgroundResource(resId);
    }

    public void setContentBackgroundColor(int color) {
        mContentFrame.setBackgroundColor(color);
    }

    public void setTitleSimple(boolean simple) {
        mIsTitleSimple = simple;
        makeTitleSimple(simple);
    }

    protected void backToMap() {
        Intent intent = mActivity.getPackageManager().getLaunchIntentForPackage(
                SharedIntent.PACKAGE_NAVIGATION);
        if (intent != null) {
            mActivity.startActivity(intent);
        }
    }

    private void checkTitleBarValid() {
        if (mIsNoTitle || mTitleBar == null) {
            throw new IllegalStateException("There is no title bar in this screen!");
        }
    }

    private void makeTitleSimple(boolean simple) {
        View goBack = mScreenView.findViewById(R.id.goBack);
        if (goBack != null) {
            goBack.setVisibility(simple ? INVISIBLE : VISIBLE);
        }
        View backToCar = mScreenView.findViewById(R.id.backToMap);
        if (backToCar != null) {
            backToCar.setVisibility(simple ? INVISIBLE : VISIBLE);
        }
    }

    private void initTitleBar() {
        // 标题栏layout尚未载入，如果再此之前不是设置为无标题栏，则将标题栏加载进来
        if (!mIsNoTitle) {
            setNoTitle(false);
        }
        View goBack = mScreenView.findViewById(R.id.goBack);
        if (goBack != null) {
            goBack.setOnClickListener(mOnClickListener);
        }
        View backToCar = mScreenView.findViewById(R.id.backToMap);
        if (backToCar != null) {
            backToCar.setOnClickListener(mOnClickListener);
        }
        makeTitleSimple(mIsTitleSimple);
    }

    private ViewGroup.LayoutParams getDefaultLayoutParams(ViewGroup.LayoutParams params) {
        return params != null ? params : new FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.goBack) {
                if (mActivity != null) {
                    mActivity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_BACK));
                    mActivity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                            KeyEvent.KEYCODE_BACK));
                }
            } else if (id == R.id.backToMap) {
                backToMap();
            }
        }
    };
}
