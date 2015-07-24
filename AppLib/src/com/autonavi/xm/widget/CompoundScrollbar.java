
package com.autonavi.xm.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.autonavi.xm.app.lib.R;
import com.autonavi.xm.view.ClickRepeater;
import com.autonavi.xm.view.ClickRepeater.OnRepeatListener;
import com.autonavi.xm.widget.ScrollView.OnScrollChangedListener;

public class CompoundScrollbar extends LinearLayout implements View.OnTouchListener,
        View.OnClickListener, OnScrollListener, OnScrollChangedListener, OnRepeatListener {

    private static final boolean CAN_SMOOTH_SCROLL = false;

    private static final int MIN_SCROLL_OFFSET_Y = 10;

    private AbsListView mAbsListView;

    private ScrollView mScrollView;

    /**
     * 滚动条可移动的区域
     */
    private final ViewGroup mScrollTrack;

    /**
     * 移动的滚动条
     */
    private final View mScrollThumb;

    private TextView mPageNumber;

    private final View mScrollUp;

    private final View mScrollDown;

    private final int mTrackScrollableHeight = -1;

    private int mVisibleCount;

    private int mTotalCount;

    private int mGridViewTotalCount;

    private int mPageCount;

    private int mThumbLastRawY;

    private boolean mIsThumbDragging = false;

    private Handler mHandler;

    /**
     * 滚动条是否可以拖动(默认为不可拖动)
     */
    private boolean mIsThumbEnabled = false;

    public CompoundScrollbar(Context context) {
        this(context, null);
    }

    public CompoundScrollbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        inflate(context, R.layout.compound_scrollbar, this);

        //        mHandler = new Handler(Looper.getMainLooper());

        mScrollThumb = findViewById(R.id.thumb);
        mScrollTrack = (ViewGroup) findViewById(R.id.track);

        mScrollThumb.setOnTouchListener(this);
        mScrollUp = findViewById(R.id.scrollUp);
        mScrollUp.setOnClickListener(this);
        ClickRepeater.attach(mScrollUp).setOnRepeatListener(this);
        mScrollDown = findViewById(R.id.scrollDown);
        mScrollDown.setOnClickListener(this);
        ClickRepeater.attach(mScrollDown).setOnRepeatListener(this);
    }

    public void setAbsListView(AbsListView absListView) {
        if (mScrollView != null) {
            mScrollView.setOnScrollChangedListener(null);
            mScrollView = null;
        }
        mAbsListView = absListView;
        if (absListView != null) {
            absListView.setOnScrollListener(this);
        }
    }

    public void setScrollView(ScrollView scrollView) {
        if (mAbsListView != null) {
            mAbsListView.setOnScrollListener(null);
            mAbsListView = null;
        }
        mScrollView = scrollView;
        if (scrollView != null) {
            mScrollView.setOnScrollChangedListener(this);
        }
    }

    public void setPageNumberEnabled(boolean enabled) {
        if (enabled) {
            findViewById(R.id.thumb_handle).setVisibility(GONE);
            mPageNumber = (TextView) findViewById(R.id.page_number);
            mPageNumber.setVisibility(VISIBLE);
            setPageNumber();
        } else {
            findViewById(R.id.thumb_handle).setVisibility(VISIBLE);
            if (mPageNumber != null) {
                mPageNumber.setVisibility(GONE);
                mPageNumber = null;
            }
        }
    }

    /**
     * 设置滚动条是否可以拖动(默认为不可拖动)
     * 
     * @param enabled true or false
     */
    public void setThumbEnabled(boolean enabled) {
        mIsThumbEnabled = enabled;
    }

    /**
     * 初始化时ListView的顶部距离,用于判断向上的按钮是否可用
     */
    private int mInitListViewTop = -999;

    /**
     * 初始化时ListView的底部距离,用于判断向下的按钮是否可用
     */
    private int mInitListViewBottom = -999;

    private void doScroll(ViewGroup view, int firstPosition, int visibleCount, int totalCount) {
        //只要visibleCount或totalCount发生改变就更新
        mVisibleCount = visibleCount;
        if (mTotalCount != totalCount) {
            mTotalCount = totalCount;
            mPageCount = visibleCount > 0 ? (int) Math.ceil((double) totalCount
                    / (double) visibleCount) : 0;
            mIsNeedToInit = true;

            if (mAbsListView != null && mAbsListView instanceof GridView) {
                GridView gridView = (GridView) mAbsListView;
                int columns = gridView.getNumColumns();
                int num = mTotalCount / columns;
                if (mTotalCount % columns == 0) {
                    mGridViewTotalCount = num;
                } else {
                    mGridViewTotalCount = num + 1;
                }
            }
        }

        if (mIsNeedToInit) {
            initTrack();
        }

        setPageNumber();

        boolean canScrollUp = false;
        boolean canScrollDown = false;

        if (totalCount > 0 && view != null) {
            if (view instanceof AbsListView) {
                //listview
                mScrollThumb.setVisibility(View.VISIBLE);
                View itemTop = view.getChildAt(0);
                View itemBottom = view.getChildAt(view.getChildCount() - 1);

                if (itemTop != null) {
                    if (mInitListViewTop == -999) {
                        mInitListViewTop = itemTop.getTop();
                    }

                    if (firstPosition > 0 || itemTop.getTop() < mInitListViewTop) {
                        canScrollUp = true;
                    }
                }

                if (itemBottom != null) {
                    if (mInitListViewTop != -999 && mInitListViewBottom == -999) {
                        mInitListViewBottom = mInitListViewTop + view.getHeight();
                    }

                    if (firstPosition + visibleCount < totalCount
                            || itemBottom.getBottom() > mInitListViewBottom) {
                        canScrollDown = true;
                    }
                }
            } else if (view instanceof ScrollView) {
                //scrollview
                mScrollThumb.setVisibility(View.VISIBLE);
                if (firstPosition > 0) {
                    canScrollUp = true;
                } else {
                    canScrollUp = false;
                }

                if (firstPosition + visibleCount < totalCount) {
                    canScrollDown = true;
                } else {
                    canScrollDown = false;
                }
            }
        }

        if (!canScrollUp) {
            mScrollUp.setEnabled(false);
        } else if (!mScrollUp.isEnabled()) {
            mScrollUp.setPressed(false);
            mScrollUp.setEnabled(true);
        }

        if (!canScrollDown) {
            mScrollDown.setEnabled(false);
        } else if (!mScrollDown.isEnabled()) {
            mScrollDown.setPressed(false);
            mScrollDown.setEnabled(true);
        }

        if (!canScrollUp && !canScrollDown) {
            mScrollThumb.setVisibility(GONE);
            mScrollThumb.setEnabled(false);
        } else if (!mScrollThumb.isEnabled()) {
            mScrollThumb.setPressed(false);
            mScrollThumb.setEnabled(true);
        }

        if (!mIsThumbDragging) {
            int trackHeight = mScrollTrack.getHeight();
            int scrollHeight = mScrollThumb.getHeight();
            if (view != null && trackHeight > 0 && scrollHeight > 0) {
                if (view instanceof AbsListView) {
                    //listview
                    View itemTop = view.getChildAt(0);
                    if (itemTop != null) {
                        int height;
                        int minus;
                        if (view instanceof GridView) {
                            int columns = ((GridView) view).getNumColumns();
                            minus = itemTop.getHeight() * mGridViewTotalCount - view.getHeight();
                            height = itemTop.getHeight() * firstPosition / columns
                                    + Math.abs(itemTop.getTop());
                        } else {
                            minus = itemTop.getHeight() * mTotalCount - view.getHeight();
                            height = itemTop.getHeight() * firstPosition
                                    + Math.abs(itemTop.getTop());
                        }
                        if (minus != 0) {
                            int moveHeight = height * (trackHeight - scrollHeight) / minus;
                            mScrollTrack.scrollTo(0, -moveHeight);
                        }
                    }
                } else if (view instanceof ScrollView) {
                    //scrollview
                    //firstPosition:滑动距离
                    int height = mScrollView.getHeight();
                    int moveHeight = firstPosition * scrollHeight / height;
                    mScrollTrack.scrollTo(0, -moveHeight);
                }
            }
        }
    }

    private void setPageNumber() {
        if (mPageNumber == null || mVisibleCount <= 0) {
            return;
        }
        int pageNum = 0;
        if (mPageCount > 0) {
            if (mAbsListView != null) {
                int firstPos = mAbsListView.getFirstVisiblePosition();
                int lastPos = mAbsListView.getLastVisiblePosition();
                if (lastPos + 1 == mTotalCount) {//FIXME 后期处理,GridView的情况
                    pageNum = mPageCount;
                } else {
                    pageNum = firstPos / mVisibleCount + 1;
                }
            } else if (mScrollView != null) {
                int sclY = mScrollView.getScrollY();
                if (sclY + mVisibleCount == mTotalCount) {
                    pageNum = mPageCount;
                } else {
                    pageNum = sclY / mVisibleCount + 1;
                }
            }
        }
        mPageNumber.setText(getResources().getString(R.string.format_page_number, pageNum,
                mPageCount));
    }

    private void handlerPostDelayed(Runnable r, long delayMillis) {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        mHandler.postDelayed(r, delayMillis);
    }

    /**
     * 记录是否需要初始化
     */
    private boolean mIsNeedToInit = true;

    private void initTrack() {
        int trackHeight = mScrollTrack.getHeight();
        if (trackHeight <= 0) {
            return;
        }

        if (mAbsListView != null) {
            //listview
            int height = mAbsListView.getHeight();
            View child = mAbsListView.getChildAt(0);

            if (height > 0 && child != null) {
                int thumbHeight;
                if (mAbsListView instanceof GridView) {
                    thumbHeight = height * trackHeight / (child.getHeight() * mGridViewTotalCount);
                } else {
                    thumbHeight = height * trackHeight / (child.getHeight() * mTotalCount);
                }
                if (thumbHeight < trackHeight / MIN_SCROLL_OFFSET_Y) {
                    thumbHeight = trackHeight / MIN_SCROLL_OFFSET_Y;
                }
                ViewGroup.LayoutParams layoutParams = mScrollThumb.getLayoutParams();
                layoutParams.height = thumbHeight;
                mScrollThumb.setLayoutParams(layoutParams);
                mScrollThumb.setMinimumHeight(thumbHeight);
                mIsNeedToInit = false;
                handlerPostDelayed(mRunnable, 200);
            }
        } else if (mScrollView != null) {
            //scrollview
            int height = mScrollView.getHeight();
            int contentHeight = mScrollView.getContentHeight();
            int thumbHeight = height * trackHeight / contentHeight;
            //            if (thumbHeight < trackHeight / MIN_SCROLL_OFFSET_Y) {
            //                thumbHeight = trackHeight / MIN_SCROLL_OFFSET_Y;
            //            }
            ViewGroup.LayoutParams layoutParams = mScrollThumb.getLayoutParams();
            layoutParams.height = thumbHeight;
            mScrollThumb.setLayoutParams(layoutParams);
            mScrollThumb.setMinimumHeight(thumbHeight);
            mIsNeedToInit = false;
            handlerPostDelayed(mRunnable, 200);

            /*
            int thumbHeight = mTotalCount > 0 ? (int) ((float) mVisibleCount * trackHeight / mTotalCount)
                    : 0;
            //            int thumbHeight = mTotalCount > 0 ? (int) ((float) mTotalCount * trackHeight / mVisibleCount)
            //                    : 0;
            thumbHeight = Math.min(trackHeight, Math.max(thumbHeight, mScrollThumb.getHeight()));

            ViewGroup.LayoutParams layoutParams = mScrollThumb.getLayoutParams();
            layoutParams.height = thumbHeight;
            mScrollThumb.setLayoutParams(layoutParams);
            mScrollThumb.setMinimumHeight(thumbHeight);
            //FIXME 暂时设置为滚动条不可拖动,这里暂时忽略
            mTrackScrollableHeight = trackHeight - thumbHeight;

            mHandler.postDelayed(mRunnable, 200);
            */
        }
    }

    /**
     * 更新Layout
     */
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            requestLayout();
        }
    };

    private void scrollContent(int step) {
        if (mAbsListView != null) {
            int fastScrollPosition = step > 0 ? mAbsListView.getLastVisiblePosition() : Math.max(0,
                    mAbsListView.getFirstVisiblePosition() + step + 1);
            if (CAN_SMOOTH_SCROLL) {
                mAbsListView.smoothScrollToPosition(fastScrollPosition);
            } else {
                if (mAbsListView instanceof ListView) {
                    ((ListView) mAbsListView).setSelection(fastScrollPosition);
                } else if (mAbsListView instanceof GridView) {
                    ((GridView) mAbsListView).setSelection(fastScrollPosition);
                }
            }
        } else if (mScrollView != null) {
            mScrollView.smoothScrollBy(0,
                    (step >= 0 ? 1 : -1) * Math.max(MIN_SCROLL_OFFSET_Y, Math.abs(step)));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // ScrollBar初始化
        if (changed) {
            if (mScrollView != null) {
                handlerPostDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doScroll(mScrollView, mScrollView.getScrollY(), mScrollView.getHeight(),
                                mScrollView.getContentHeight());
                    }
                }, 200);
            } else {
                initTrack();
            }
        }
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.scrollUp) {
            scrollContent(-mVisibleCount);
        } else if (id == R.id.scrollDown) {
            scrollContent(mVisibleCount);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mIsThumbEnabled) {
            int action = event.getAction();
            int y = (int) event.getRawY();
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    mIsThumbDragging = true;
                    mThumbLastRawY = y;
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    int deltaY = mThumbLastRawY - y;
                    int trackScrollY = mScrollTrack.getScrollY() + deltaY;
                    trackScrollY = Math.max(-mTrackScrollableHeight, Math.min(0, trackScrollY));
                    mScrollTrack.scrollTo(0, trackScrollY);
                    int firstPosition = -(int) ((float) trackScrollY / mTrackScrollableHeight * mTotalCount);
                    if (mAbsListView != null) {
                        if (mAbsListView instanceof ListView) {
                            ((ListView) mAbsListView).setSelection(firstPosition);
                        } else if (mAbsListView instanceof GridView) {
                            firstPosition = -(int) ((float) trackScrollY / mTrackScrollableHeight * mGridViewTotalCount);
                            ((GridView) mAbsListView).setSelection(firstPosition);
                        } else {
                            if (CAN_SMOOTH_SCROLL) {
                                mAbsListView.smoothScrollToPosition(firstPosition);
                            }
                        }
                    } else if (mScrollView != null) {
                        mScrollView.scrollTo(0, firstPosition);
                    }
                    mThumbLastRawY = y;
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    mIsThumbDragging = false;
                    break;
                }
            }
        }
        return false;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        doScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScrollChanged(ScrollView scrollView, int l, int t, int oldl, int oldt) {
        doScroll(scrollView, t, mScrollView.getHeight(), mScrollView.getContentHeight());
    }

    @Override
    public void onRepeatStart(View view) {
    }

    @Override
    public void onRepeat(View view) {
        int id = view.getId();
        if (id == R.id.scrollUp) {
            scrollContent(-1);
        } else if (id == R.id.scrollDown) {
            scrollContent(1);
        }
    }

    @Override
    public void onRepeatEnd(View view) {
    }

}
