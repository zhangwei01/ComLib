
package com.autonavi.xm.app;

import android.view.KeyEvent;
import android.view.MotionEvent;

/* package */interface BaseFragmentCallback {

    public void onStartView();

    public void onResumeView();

    public void onPauseView();

    public void onStopView();

    public boolean dispatchTouchEvent(MotionEvent event);

    public boolean onTouchEvent(MotionEvent event);

    public boolean dispatchKeyEvent(KeyEvent event);

    public boolean onKeyDown(int keyCode, KeyEvent event);

    public boolean onKeyUp(int keyCode, KeyEvent event);

    /**
     * 用户按下 返回 按键时触发
     * 
     * @return true则拦截此事件自行处理，否则返回false
     */
    public boolean onBackPressed();

}
