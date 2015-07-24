
package com.autonavi.xm.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import java.util.ArrayList;

public class MultiScreen extends ViewGroup {

    public static final int DEFAULT_SCREEN_MIDDLE = -1;

    public static final int INVALID_SCREEN = -1;

    private static final int SNAP_VELOCITY = 1000;

    private static final int TOUCH_STATE_REST = 0;

    private static final int TOUCH_STATE_SCROLLING = 1;

    private static final int SCROLL_STATE_REST = 0;

    private static final int SCROLL_STATE_SCROLLING = 1;

    private int mTouchState = TOUCH_STATE_REST;

    private int mScrollState = SCROLL_STATE_REST;

    private int mExtendEdgeSpace = 0;

    private int mDefaultScreen = DEFAULT_SCREEN_MIDDLE;

    private int mCurrentScreen = INVALID_SCREEN;

    private int mNextScreen = INVALID_SCREEN;

    private Scroller mScroller;

    private int mTouchSlop;

    private int mMaximumVelocity;

    private OnScreenChangeListener mOnScreenChangeListener;

    private boolean mFadingEdgeEnabled = true;

    private boolean mTouchScrollEnabled = true;

    private boolean mFastSnapEnabled = true;

    private boolean mScreenLocked = false;

    private boolean mCycleScrollEnabled = true;

    private int mCycleScreen = INVALID_SCREEN;

    public MultiScreen(Context context) {
        this(context, null, 0);
    }

    public MultiScreen(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiScreen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initMultiScreen();
    }

    private void initMultiScreen() {
        setWillNotDraw(false);
        mScroller = new Scroller(getContext());
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    public int getCurrentScreen() {
        return mCurrentScreen;
    }

    public int getNextScreen() {
        return mNextScreen;
    }

    public int getScreenCount() {
        return getChildCount();
    }

    public void setCurrentScreen(int currentScreen) {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            mNextScreen = INVALID_SCREEN;
        }
        mCurrentScreen = Math.max(0, Math.min(currentScreen, getChildCount() - 1));
        scrollTo(mCurrentScreen * getWidth(), 0);
        invalidate();
    }

    public int getDefaultScreen() {
        return mDefaultScreen;
    }

    public void setDefaultScreen(int defaultScreen) {
        mDefaultScreen = Math.max(0, Math.min(defaultScreen, getChildCount() - 1));
    }

    private int computeDefaultScreen() {
        int count = getChildCount();
        return mDefaultScreen == DEFAULT_SCREEN_MIDDLE ? (int) Math.floor(count / 2.0 - 0.5) : Math
                .max(0, Math.min(count - 1, mDefaultScreen));
    }

    public void showDefaultScreen() {
        setCurrentScreen(computeDefaultScreen());
    }

    public void setOnScreenChangeListener(OnScreenChangeListener listener) {
        mOnScreenChangeListener = listener;
    }

    public void snapLeft() {
        int childCount;
        int screenToCheck = mNextScreen != INVALID_SCREEN ? mNextScreen : mCurrentScreen;
        if (screenToCheck > 0) {
            snapToScreen(screenToCheck - 1);
        } else if (mCycleScrollEnabled && (childCount = getChildCount()) > 1) {
            mCycleScreen = childCount - 1;
            layoutChildren(LAYOUT_TYPE_LAST_MOVE_TO_FIRST);
            scrollBy(getWidth(), 0);
            snapToScreen(0);
        }
    }

    public void snapRight() {
        int childCount = getChildCount();
        int screenToCheck = mNextScreen != INVALID_SCREEN ? mNextScreen : mCurrentScreen;
        if (screenToCheck < childCount - 1) {
            snapToScreen(screenToCheck + 1);
        } else if (mCycleScrollEnabled && childCount > 1) {
            mCycleScreen = 0;
            layoutChildren(LAYOUT_TYPE_FIRST_MOVE_TO_LAST);
            scrollBy(-getWidth(), 0);
            snapToScreen(childCount - 1);
        }
    }

    private boolean mFirstMeasure = true;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mFirstMeasure) {
            if (mCurrentScreen == INVALID_SCREEN) {
                mCurrentScreen = computeDefaultScreen();
            }
            mFirstMeasure = false;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mExtendEdgeSpace = w / 2;
        scrollTo(mCurrentScreen * w, 0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutChildren(LAYOUT_TYPE_NORMAL);
    }

    private static final int LAYOUT_TYPE_NORMAL = 0;

    private static final int LAYOUT_TYPE_FIRST_MOVE_TO_LAST = 1;

    private static final int LAYOUT_TYPE_LAST_MOVE_TO_FIRST = 2;

    private void layoutChildren(int type) {
        int childLeft = 0;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            int index;
            switch (type) {
                case LAYOUT_TYPE_FIRST_MOVE_TO_LAST:
                    index = (i + 1) % count;
                    break;
                case LAYOUT_TYPE_LAST_MOVE_TO_FIRST:
                    index = (i + count - 1) % count;
                    break;
                case LAYOUT_TYPE_NORMAL:
                default:
                    index = i;
                    break;
            }
            View child = getChildAt(index);
            if (child.getVisibility() != View.GONE) {
                int childWidth = child.getMeasuredWidth();
                child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        int width = getWidth();
        if (width == 0) {
            return;
        }
        int focusedLeft = focused.getLeft();
        View current = focused;
        do {
            View parent = (View) current.getParent();
            focusedLeft += parent.getLeft();
            current = parent;
        } while (current != null && current != this);
        int scrollX = getScrollX();
        if (focusedLeft >= scrollX + width) {
            snapToScreen(focusedLeft / width);
        } else if (focusedLeft + focused.getWidth() <= scrollX) {
            snapToScreen((focusedLeft + focused.getWidth()) / width);
        }
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        int screen = indexOfChild(child);
        if (screen != mCurrentScreen || !mScroller.isFinished()) {
            snapToScreen(screen);
            return true;
        }
        return false;
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        int focusableScreen;
        if (mNextScreen != INVALID_SCREEN) {
            focusableScreen = mNextScreen;
        } else {
            focusableScreen = mCurrentScreen;
        }
        View focusableView = getChildAt(focusableScreen);
        if (focusableView != null) {
            focusableView.requestFocus(direction, previouslyFocusedRect);
        }
        return true;
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        if (direction == View.FOCUS_LEFT) {
            if (mCurrentScreen > 0) {
                snapLeft();
                return true;
            }
        } else if (direction == View.FOCUS_RIGHT) {
            if (mCurrentScreen < getChildCount() - 1) {
                snapRight();
                return true;
            }
        }
        return super.dispatchUnhandledMove(focused, direction);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (getChildCount() <= 0 || mCurrentScreen == INVALID_SCREEN) {
            super.addFocusables(views, direction, focusableMode);
            return;
        }
        int currentScreen = mCurrentScreen;
        getChildAt(currentScreen).addFocusables(views, direction);
        if (direction == View.FOCUS_LEFT) {
            if (currentScreen > 0) {
                getChildAt(currentScreen - 1).addFocusables(views, direction);
            }
        } else if (direction == View.FOCUS_RIGHT) {
            if (currentScreen < getChildCount() - 1) {
                getChildAt(currentScreen + 1).addFocusables(views, direction);
            }
        }
    }

    @Override
    public void computeScroll() {
        Scroller scroller = mScroller;
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        } else if (mNextScreen != INVALID_SCREEN) {
            int lastCurrentScreen = mCurrentScreen;
            int currentScreen = INVALID_SCREEN;
            if (mCycleScrollEnabled && mCycleScreen != INVALID_SCREEN) {
                currentScreen = mCurrentScreen = mCycleScreen;
                layoutChildren(LAYOUT_TYPE_NORMAL);
                scrollTo(currentScreen * getWidth(), 0);
                mCycleScreen = INVALID_SCREEN;
            } else {
                currentScreen = mCurrentScreen = Math.max(0,
                        Math.min(mNextScreen, getChildCount() - 1));
            }
            mNextScreen = INVALID_SCREEN;

            if (mFadingEdgeEnabled) {
                setHorizontalFadingEdgeEnabled(false);
            }
            onScreenScrollEnd();
            if (lastCurrentScreen != currentScreen) {
                onScreenChangeEnd(currentScreen);
            }
        }
    }

    protected void onScreenScrollStart() {
        if (mScrollState == SCROLL_STATE_SCROLLING) {
            return;
        }
        mScrollState = SCROLL_STATE_SCROLLING;
        OnScreenChangeListener listener = mOnScreenChangeListener;
        if (listener != null) {
            listener.onScreenScrollStart();
        }
    }

    protected void onScreenScrollEnd() {
        if (mScrollState == SCROLL_STATE_REST) {
            return;
        }
        mScrollState = SCROLL_STATE_REST;
        OnScreenChangeListener listener = mOnScreenChangeListener;
        if (listener != null) {
            listener.onScreenScrollEnd();
        }
    }

    protected void onScreenChangeStart(int nextscreen) {
        OnScreenChangeListener listener = mOnScreenChangeListener;
        if (listener != null) {
            listener.onScreenChangeStart(nextscreen);
        }
    }

    protected void onScreenChangeEnd(int currentscreen) {
        OnScreenChangeListener listener = mOnScreenChangeListener;
        if (listener != null) {
            listener.onScreenChangeEnd(currentscreen);
        }
    }

    public void setFadingEdgeEnabled(boolean fadingEdgeEnabled) {
        mFadingEdgeEnabled = fadingEdgeEnabled;
    }

    public void setTouchScrollEnabled(boolean touchScrollEnabled) {
        mTouchScrollEnabled = touchScrollEnabled;
    }

    public void setCycleScrollEnabled(boolean cycleScrollEnabled) {
        mCycleScrollEnabled = cycleScrollEnabled;
    }

    public void setFastSnapEnabled(boolean fastSnapEnabled) {
        mFastSnapEnabled = fastSnapEnabled;
    }

    public void setScreenLocked(boolean screenLocked) {
        mScreenLocked = screenLocked;
    }

    public void setExtendEdgeSpace(int extendEdgeSpace) {
        mExtendEdgeSpace = extendEdgeSpace;
    }

    @Override
    protected int computeHorizontalScrollRange() {
        return getWidth() * getChildCount();
    }

    private float mLastMotionX;

    private float mLastMotionY;

    private boolean mAllowLongPress;

    private VelocityTracker mVelocityTracker;

    private boolean mIsAbort = false;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!mTouchScrollEnabled) {
            return super.dispatchTouchEvent(event);
        }
        int childCount = getChildCount();
        if (childCount <= 0) {
            return super.dispatchTouchEvent(event);
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mIsAbort = false;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    mIsAbort = true;
                }
                mLastMotionX = x;
                mLastMotionY = y;
                mAllowLongPress = true;
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
                break;
            case MotionEvent.ACTION_MOVE:
                int xDiff = (int) Math.abs(x - mLastMotionX);
                int yDiff = (int) Math.abs(y - mLastMotionY);
                int touchSlop = mTouchSlop;
                boolean xMoved = xDiff > touchSlop;
                boolean yMoved = yDiff > touchSlop;

                if (xMoved || yMoved) {
                    int lastTouchState = mTouchState;
                    if (xMoved) {
                        mTouchState = TOUCH_STATE_SCROLLING;
                        if (lastTouchState != TOUCH_STATE_SCROLLING) {
                            onScreenScrollStart();
                        }
                    }
                    if (mAllowLongPress) {
                        mAllowLongPress = false;
                        View currentScreen = getChildAt(mCurrentScreen);
                        if (currentScreen != null) {
                            currentScreen.cancelLongPress();
                        }
                    }
                }
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    int deltaX = (int) (mLastMotionX - x);
                    if (mFadingEdgeEnabled) {
                        setHorizontalFadingEdgeEnabled(true);
                    }
                    int extendEdgeSpace = mCycleScrollEnabled && childCount > 1 ? 0
                            : mExtendEdgeSpace;
                    mLastMotionX = x;
                    int scrollX = getScrollX();
                    if (deltaX < 0) {
                        if (scrollX > -extendEdgeSpace) {
                            scrollBy(Math.max(-extendEdgeSpace - scrollX, scrollX > 0 ? deltaX
                                    : deltaX / 2), 0);
                        } else if (mCycleScrollEnabled && childCount > 1) {
                            mCycleScreen = childCount - 1;
                            layoutChildren(LAYOUT_TYPE_LAST_MOVE_TO_FIRST);
                            scrollBy(getWidth() + deltaX, 0);
                        }
                    } else if (deltaX > 0) {
                        int screenWidth = getWidth();
                        int availableToScroll = screenWidth * childCount - scrollX - screenWidth
                                + extendEdgeSpace;
                        if (availableToScroll > 0) {
                            int offset = availableToScroll - extendEdgeSpace;
                            scrollBy(Math.min(availableToScroll, offset > 0 ? deltaX : deltaX / 2),
                                    0);
                        } else if (mCycleScrollEnabled && childCount > 1) {
                            mCycleScreen = 0;
                            layoutChildren(LAYOUT_TYPE_FIRST_MOVE_TO_LAST);
                            scrollBy(-screenWidth + deltaX, 0);
                        }
                    }
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    super.dispatchTouchEvent(event);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int velocityX = (int) velocityTracker.getXVelocity();

                    if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
                        snapToScreen(mCurrentScreen - 1);
                    } else if (velocityX < -SNAP_VELOCITY && mCurrentScreen < childCount - 1) {
                        snapToScreen(mCurrentScreen + 1);
                    } else {
                        snapToDestination();
                    }
                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                } else if (mIsAbort) {
                    mIsAbort = false;
                    snapToDestination();
                }
                mTouchState = TOUCH_STATE_REST;
                mAllowLongPress = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_REST;
                mAllowLongPress = false;
                break;
        }
        super.dispatchTouchEvent(event);
        return true;
    }

    public void snapToScreen(int whichScreen) {
        snapToScreen(whichScreen, true);
    }

    public void snapToScreen(int whichScreen, boolean notifyScroll) {
        if (mScreenLocked) {
            return;
        }
        if (!mFastSnapEnabled) {
            if (!mScroller.isFinished()) {
                return;
            }
        } else if (mFastSnapEnabled && mNextScreen != INVALID_SCREEN) {
            mCurrentScreen = mNextScreen;
        }
        int currentScreen = mCurrentScreen;
        int cycleScreen = mCycleScreen;
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        boolean changingScreens = (mCycleScrollEnabled && cycleScreen != INVALID_SCREEN) ? cycleScreen != currentScreen
                : whichScreen != currentScreen;

        mNextScreen = whichScreen;

        View focusedChild = getFocusedChild();
        if (focusedChild != null && changingScreens && focusedChild == getChildAt(currentScreen)) {
            focusedChild.clearFocus();
        }

        int newX = whichScreen * getWidth();
        int delta = newX - getScrollX();
        if (delta == 0) {
            return;
        }
        if (mFadingEdgeEnabled) {
            setHorizontalFadingEdgeEnabled(true);
        }
        mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);
        invalidate();
        if (notifyScroll) {
            onScreenScrollStart();
        }
        if (changingScreens) {
            onScreenChangeStart((mCycleScrollEnabled && cycleScreen != INVALID_SCREEN) ? cycleScreen
                    : whichScreen);
        }
    }

    private void snapToDestination() {
        int screenWidth = getWidth();
        int whichScreen = (getScrollX() + screenWidth / 2) / screenWidth;
        if (mCycleScrollEnabled && mCycleScreen != INVALID_SCREEN) {
            if (whichScreen != mCurrentScreen) {
                mCycleScreen = mCycleScreen == 0 ? getChildCount() - 1 : 0;
            }
        }
        snapToScreen(whichScreen, false);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.currentScreen = mCurrentScreen;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        if (savedState.currentScreen != -1) {
            mCurrentScreen = savedState.currentScreen;
        }
    }

    public static interface OnScreenChangeListener {

        public void onScreenScrollStart();

        public void onScreenScrollEnd();

        public void onScreenChangeStart(int nextscreen);

        public void onScreenChangeEnd(int currentscreen);
    }

    public static class SavedState extends BaseSavedState {

        int currentScreen = -1;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentScreen = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(currentScreen);
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
