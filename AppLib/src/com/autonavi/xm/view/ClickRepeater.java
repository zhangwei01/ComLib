
package com.autonavi.xm.view;

import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;

public class ClickRepeater implements OnTouchListener {

    private static final int REPEAT_TIMEOUT = ViewConfiguration.getLongPressTimeout();

    private static final int DEFAULT_REPEAT_DELAY = 30;

    private static final int WHAT_REPEAT = 0;

    private View mHostView;

    private int mRepeatDelay;

    private boolean mIsRepeating = false;

    private OnRepeatListener mOnRepeatListener;

    public ClickRepeater(View view) {
        this(view, DEFAULT_REPEAT_DELAY, null);
    }

    public ClickRepeater(View view, int repeatDelay) {
        this(view, repeatDelay, null);
    }

    public ClickRepeater(View view, int repeatInterval, OnRepeatListener listener) {
        mHostView = view;
        mRepeatDelay = repeatInterval;
        mOnRepeatListener = listener;
        view.setOnTouchListener(this);
    }

    public ClickRepeater setOnRepeatListener(OnRepeatListener listener) {
        mOnRepeatListener = listener;
        return this;
    }

    public ClickRepeater setRepeatDelay(int delay) {
        if (delay > 0) {
            mRepeatDelay = delay;
        }
        return this;
    }

    public void start() {
        mUiHandler.sendEmptyMessage(WHAT_REPEAT);
    }

    public void stop() {
        mUiHandler.removeMessages(WHAT_REPEAT);
        if (mIsRepeating) {
            if (mOnRepeatListener != null) {
                mOnRepeatListener.onRepeatEnd(mHostView);
            }
            mIsRepeating = false;
        }
    }

    public static ClickRepeater attach(View view) {
        return new ClickRepeater(view);
    }

    public static ClickRepeater attach(View view, int repeatDelay) {
        return new ClickRepeater(view, repeatDelay);
    }

    public static ClickRepeater attach(View view, int repeatDelay, OnRepeatListener listener) {
        return new ClickRepeater(view, repeatDelay, listener);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mUiHandler
                        .sendEmptyMessageAtTime(WHAT_REPEAT, event.getDownTime() + REPEAT_TIMEOUT);
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                stop();
                break;
            }
        }
        return false;
    }

    private Handler mUiHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_REPEAT) {
                if (mHostView.isEnabled()) {
                    if (!mIsRepeating) {
                        if (mOnRepeatListener != null) {
                            mOnRepeatListener.onRepeatStart(mHostView);
                        }
                        mIsRepeating = true;
                    }
                    if (mOnRepeatListener != null) {
                        mOnRepeatListener.onRepeat(mHostView);
                    } else {
                        mHostView.performClick();
                    }
                    sendEmptyMessageDelayed(WHAT_REPEAT, mRepeatDelay);
                } else {
                    stop();
                }
            }
        }

    };

    public static interface OnRepeatListener {

        public void onRepeatStart(View view);

        public void onRepeat(View view);

        public void onRepeatEnd(View view);
    }
}
