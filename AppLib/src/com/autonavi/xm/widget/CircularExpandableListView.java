
package com.autonavi.xm.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

/**
 * 循环拓展列表，目的跟CircularListView是一样的，都是要完成在没有其他可focusable的时候完成自循环，
 * 有其他可focusable的控件时则跳出，backforward就直接进入列表的尾部
 * 
 * @author changchun.cai
 */
public class CircularExpandableListView extends ExpandableListView implements
        OnGroupExpandListener, OnGroupCollapseListener {

    public CircularExpandableListView(Context context) {
        super(context);
    }

    public CircularExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircularExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (getCount() == 0) {
            return;
        }
        //分最后一个goup是否有展开两种情况，展开则进入子列表最后一项，相反则进入最后一个group
        if (gainFocus && direction == FOCUS_BACKWARD) {
            int groupCount = getExpandableListAdapter().getGroupCount();
            int groupPosition = groupCount - 1;
            boolean isGroupExpanded = isGroupExpanded(groupPosition);
            if (isGroupExpanded) {
                int childCount = getExpandableListAdapter().getChildrenCount(groupPosition);
                int childPosition = childCount - 1;
                setSelectedChild(groupPosition, childPosition, true);
            } else {
                setSelectedGroup(groupPosition);
            }
        }
        //FOCUS_FORWARD则进入第一个group
        if (gainFocus && direction == FOCUS_FORWARD) {
            setSelectedGroup(0);
        }

    }

    private boolean mIsCircular = false;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_TAB
                    && (event.getMetaState() | KeyEvent.META_SHIFT_MASK) == KeyEvent.META_SHIFT_MASK) {
                event = new KeyEvent(event.getDownTime(), event.getEventTime(), event.getAction(),
                        event.isShiftPressed() ? KeyEvent.KEYCODE_DPAD_UP
                                : KeyEvent.KEYCODE_DPAD_DOWN, event.getRepeatCount(), 0,
                        event.getDeviceId(), event.getScanCode(), event.getFlags(),
                        event.getSource());
                mIsCircular = true;
                if (onKeyDown(keyCode, event)) {
                    return true;
                }
            }
        }
        mIsCircular = false;
        return super.dispatchKeyEvent(event);

    }

    private long mPrePosition = 0;

    private boolean hasOutsideFocusableView(int direction) {
        View view = focusSearch(direction);
        if (view == this) {
            return false;
        }
        ViewParent parent = view.getParent();
        for (; parent != null; parent = parent.getParent()) {
            if (parent == this) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mIsCircular && keyCode == KeyEvent.KEYCODE_TAB) {
            int keycode = event.getKeyCode();
            int groupPosition = getExpandableListAdapter().getGroupCount() - 1;
            int childPosition = getExpandableListAdapter().getChildrenCount(groupPosition);
            boolean isLastGroupExpanded = isGroupExpanded(groupPosition);
            if (keycode == KeyEvent.KEYCODE_DPAD_UP) {
                if (isLastGroupExpanded) {
                    if (getSelectedItemPosition() - 1 < 0
                            && !hasOutsideFocusableView(View.FOCUS_BACKWARD) && mPrePosition == 0) {
                        setSelectedChild(groupPosition, childPosition, true);
                        mPrePosition = getSelectedItemPosition();
                        return true;
                    }

                } else {
                    if (getSelectedItemPosition() - 1 < 0
                            && !hasOutsideFocusableView(View.FOCUS_BACKWARD) && mPrePosition == 0) {
                        setSelectedGroup(groupPosition);
                        mPrePosition = getSelectedItemPosition();
                        return true;
                    }

                }

            } else if (keycode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (isLastGroupExpanded) {
                    View lastChildView = getChildAt(getChildCount() - 1);
                    if (getSelectedView() == lastChildView
                            && !hasOutsideFocusableView(View.FOCUS_FORWARD)) {
                        setSelectedGroup(0);
                        mPrePosition = getSelectedItemPosition();
                        return true;
                    }
                } else {
                    if (getSelectedItemPosition() + 1 > groupPosition
                            && !hasOutsideFocusableView(View.FOCUS_FORWARD)
                            && mPrePosition == groupPosition) {
                        setSelectedGroup(0);
                        mPrePosition = getSelectedItemPosition();
                        return true;
                    }
                }

            }
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            mPrePosition = getSelectedItemPosition() + 1;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            mPrePosition = getSelectedItemPosition() - 1;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 需要在最后一个groupPostion的时候对上一次的位置做保留的原因是如果没有保存，当展开，并到最后一项的时候，点击返回到最后一个group，
     * 点击关闭group这时候右旋会出现位置出错而不跳到第一项。
     **/
    private long mPrePositionTemp;

    @Override
    public void onGroupExpand(int groupPosition) {
        if (groupPosition == getExpandableListAdapter().getGroupCount() - 1) {
            mPrePositionTemp = mPrePosition;
        }
    }

    @Override
    public void onGroupCollapse(int groupPosition) {
        if (groupPosition == getExpandableListAdapter().getGroupCount() - 1) {
            mPrePosition = mPrePositionTemp;
        }
    }

}
