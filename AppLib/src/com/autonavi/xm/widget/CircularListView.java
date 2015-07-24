
package com.autonavi.xm.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.ListView;

/**
 * 当界面上只有该ListView，且只有该ListView是Foucsable时，该ListView会自动循环列表，如果有其它Focusable控件，
 * 则会跳出，不会循环。
 * 
 * @see #onFocusChanged(boolean, int, Rect)
 *      目的是控制光标左旋时直接跳到列表的最后一行，并在该列表失去光标的时候记录之前选定的位置
 * @see #dispatchKeyEvent(KeyEvent) 将tab键和tab+shift分别转换为光标右旋和光标左旋
 * @see #onKeyDown(int, KeyEvent) 处理是否循环列表的逻辑
 * @author changchun.cai
 */
public class CircularListView extends ListView {
    public CircularListView(Context context) {
        super(context);
    }

    public CircularListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircularListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (getCount() == 0) {
            return;
        }
        //左旋:1 右旋:2 都不是则为0
        int focusDirection = 0;

        //目的是为了在右旋的时候进入第一个位置，需要判断previouslyFocusedRect是否为空的目的是，有可能是从其他界面跳转进入
        //时，有可能该ListView也会受到FOCUS_BACKWARD，不过此时previouslyFocusedRect为空。
        //而在同一个界面里面跳转时，previouslyFocusedRect不会为空。
        if (gainFocus && direction == FOCUS_BACKWARD && previouslyFocusedRect != null) {
            setSelection(getCount() - 1);
            focusDirection = 1;
        }

        //目的是为了在右旋的时候进入最后一个位置，需要判断previouslyFocusedRect是否为空的目的是，有可能是从其他界面跳转进入
        //时，有可能该ListView也会受到FOCUS_BACKWARD，不过此时previouslyFocusedRect为空。
        //而在同一个界面里面跳转时，previouslyFocusedRect不会为空。
        if (gainFocus && direction == FOCUS_FORWARD && previouslyFocusedRect != null) {
            setSelection(0);
            focusDirection = 2;
        }

        if (!gainFocus && getSelectedItemPosition() != INVALID_POSITION) {
            mPrePosition = getSelectedItemPosition();
        }

        if (mOnFocusChangeListener != null) {
            mOnFocusChangeListener.onFocusChange(gainFocus, focusDirection);
        }
    }

    private OnFocusChangeListener mOnFocusChangeListener;

    /**
     * Listview焦点改变的监听器
     * 
     * @author hanwei.chen
     * @since 2013-10-18 下午4:45:40
     */
    public interface OnFocusChangeListener {
        public void onFocusChange(boolean focused, int direction);
    }

    /**
     * 设置自定义Listview焦点改变的监听
     */
    public void setOnFocusChangeListener(OnFocusChangeListener listener) {
        mOnFocusChangeListener = listener;
    }

    /**
     * 标明需要循环列表
     */
    private boolean mIsCircular = false;

    /**
     * 将tab键和tab+shift分别转换为光标右旋和光标左旋,@see {@link #mIsCircular}
     */
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

    private int mPrePosition = 0;

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

    private View mConvertView;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.ACTION_DOWN == event.getAction()) {
            if (getSelectedItemPosition() == INVALID_POSITION) {
                return super.onKeyDown(keyCode, event);
            }
            //这边的keyCode是由 dispatchKeyEvent(KeyEventevent)转换左旋右旋之后再次调用onKeyDown的keyCode,
            //传的值是转换之前的原始值
            if (mIsCircular && keyCode == KeyEvent.KEYCODE_TAB) {
                int keycode = event.getKeyCode();
                int lastPosition = getCount() - 1;
                if (keycode == KeyEvent.KEYCODE_DPAD_UP) {
                    //向上
                    if (getSelectedItemPosition() - 1 < 0
                            && !hasOutsideFocusableView(View.FOCUS_BACKWARD) && mPrePosition == 0) {
                        mConvertView = getAdapter().getView(lastPosition, mConvertView, this);
                        //防止最后一栏是disabled的状态
                        if (mConvertView != null && !mConvertView.isEnabled()) {
                            return false;
                        }
                        setSelection(lastPosition);
                        mPrePosition = getSelectedItemPosition();
                        return true;
                    }
                    mPrePosition = getSelectedItemPosition() - 1;
                } else if (keycode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    //向下
                    if (getSelectedItemPosition() + 1 > lastPosition
                            && !hasOutsideFocusableView(View.FOCUS_FORWARD)
                            && mPrePosition == lastPosition) {
                        //防止第一栏是disabled的状态
                        mConvertView = getAdapter().getView(0, mConvertView, this);
                        if (mConvertView != null && !mConvertView.isEnabled()) {
                            return false;
                        }
                        setSelection(0);
                        mPrePosition = getSelectedItemPosition();
                        return true;
                    }
                    mPrePosition = getSelectedItemPosition() + 1;
                }
            }

            //向下键，向上键直接操作时也要记录当前选中的位置
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                //向下
                if (!(getSelectedItemPosition() + 1 > getCount() - 1)) {
                    mPrePosition = getSelectedItemPosition() + 1;
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                //向上
                if (!(getSelectedItemPosition() - 1 <= -1)) {
                    mPrePosition = getSelectedItemPosition() - 1;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
