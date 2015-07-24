
package com.autonavi.xm.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.autonavi.xm.content.SharedIntent;
import com.autonavi.xm.view.BaseScreen;
import com.autonavi.xm.widget.BaseToast;

import java.util.ListIterator;
import java.util.Stack;

public class BaseActivity extends FragmentActivity implements BaseDialogFragmentCallback {

    private static final String DIALOG_FRAGMENT_TAG = "com.autonavi.xm.tag.DialogFragment:%1$d-%2$d";

    private BaseScreen mBaseScreen;

    private BaseApplication mBaseApplication;

    private FragmentManager mFragmentManager;

    private Stack<BaseFragment> mFragmentStack;

    private BaseToast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFragmentStack = new Stack<BaseFragment>();

        super.onCreate(savedInstanceState);

        mBaseApplication = (BaseApplication) getApplication();
        mBaseApplication.pushActivity(this);

        mFragmentManager = getSupportFragmentManager();

        if (isScreenViewEnabled()) {
            mBaseScreen = new BaseScreen(this);
            super.setContentView(mBaseScreen.getScreenView());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        if (intent != null) {
            Bundle savedState = intent.getBundleExtra(SharedIntent.EXTRA_SAVED_INSTANCE_STATE);
            if (savedState != null) {
                onRestoreInstanceState(savedState);
                intent.removeExtra(SharedIntent.EXTRA_SAVED_INSTANCE_STATE);
            }
        }

        if (isStatisticsAgentEnabled()) {
            ((BaseApplication) getApplication()).getStatisticsAgent().start();
        }
    }

    @Override
    protected void onPause() {
        if (isFinishing()) {
            mBaseApplication.removeActivity(this);
        }
        if (isStatisticsAgentEnabled()) {
            ((BaseApplication) getApplication()).getStatisticsAgent().stop();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mBaseApplication.removeActivity(this);
        super.onDestroy();
    }

    @Override
    public void finish() {
        mBaseApplication.removeActivity(this);
        super.finish();
    }

    protected boolean isScreenViewEnabled() {
        return false;
    }

    protected void restartStackedFragments() {
        BaseFragment[] fragments = getStackedFragments();
        if (fragments == null || fragments.length <= 0) {
            return;
        }
        int len = fragments.length;
        Fragment.SavedState[] savedStates = new Fragment.SavedState[len];
        FragmentManager fmng = getSupportFragmentManager();
        for (int i = 0; i < len; i++) {
            savedStates[i] = fmng.saveFragmentInstanceState(fragments[i]);
        }
        fmng.popBackStackImmediate(0, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        for (int i = 0; i < len; i++) {
            Intent intent = new Intent();
            if (savedStates[i] != null) {
                intent.putExtra(SharedIntent.EXTRA_SAVED_INSTANCE_STATE, savedStates[i]);
            }
            intent.setClass(this, fragments[i].getClass());
            startFragment(intent);
        }
        //立即执行，防止重建时界面闪动
        fmng.executePendingTransactions();
        System.gc();
    }

    protected BaseActivity[] getStackedActivities() {
        return mBaseApplication.getStackedActivities();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        BaseFragment last = getPreviousFragment(null);
        if (last != null) {
            if (last.dispatchTouchEvent(event)) {
                return true;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        BaseFragment last = getPreviousFragment(null);
        if (last != null) {
            if (last.onTouchEvent(event)) {
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        BaseFragment last = getPreviousFragment(null);
        if (last != null) {
            if (last.dispatchKeyEvent(event)) {
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        BaseFragment last = getPreviousFragment(null);
        if (last != null) {
            if (last.onKeyDown(keyCode, event)) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        BaseFragment last = getPreviousFragment(null);
        if (last != null) {
            if (last.onKeyUp(keyCode, event)) {
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        BaseFragment last = getPreviousFragment(null);
        if (last != null) {
            if (last.onBackPressed()) {
                return;
            }
        }
        super.onBackPressed();
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param notitle true为无标题，false有带标题
     */
    public void setNoTitle(boolean notitle) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setNoTitle(notitle);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitle(title);
        }
    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitle(titleId);
        }
    }

    protected void setTitleIcon(int resId) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleIcon(resId);
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param layoutResId
     */
    public void setTitleContent(int layoutResId) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleContent(layoutResId);
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param view
     */
    public void setTitleContent(View view) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleContent(view);
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param view
     * @param params
     */
    public void setTitleContent(View view, ViewGroup.LayoutParams params) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleContent(view, params);
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param layoutResId
     */
    public void setTitleRightContent(int layoutResId) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleRightContent(layoutResId);
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param view
     */
    public void setTitleRightContent(View view) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleRightContent(view);
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param view
     * @param params
     */
    public void setTitleRightContent(View view, ViewGroup.LayoutParams params) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleRightContent(view, params);
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param layoutResId
     */
    public void setTitleLeftContent(int layoutResId) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleLeftContent(layoutResId);
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param view
     */
    public void setTitleLeftContent(View view) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleLeftContent(view);
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param view
     * @param params
     */
    public void setTitleLeftContent(View view, ViewGroup.LayoutParams params) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleLeftContent(view, params);
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param simple
     */
    public void setTitleSimple(boolean simple) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleSimple(simple);
        }
    }

    @Override
    public void setContentView(int layoutResId) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setContentView(layoutResId);
        } else {
            super.setContentView(layoutResId);
        }
    }

    @Override
    public void setContentView(View view) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setContentView(view);
        } else {
            super.setContentView(view);
        }
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setContentView(view, params);
        } else {
            super.setContentView(view, params);
        }
    }

    public void showToast(int textId) {
        showToast(textId, 0);
    }

    public void showToast(String text) {
        if (mToast == null) {
            mToast = new BaseToast(this);
        }
        mToast.show(text);
    }

    public void showToast(int textId, int iconId) {
        if (mToast == null) {
            mToast = new BaseToast(this);
        }
        mToast.show(textId, iconId);
    }

    public void cancelToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    /* package */static String makeDialogFragmentTag(int backStackId, int id) {
        return String.format(DIALOG_FRAGMENT_TAG, backStackId, id);
    }

    protected void showDialogFragment(BaseDialogFragment dialogFragment, int id) {
        if (mFragmentManager.findFragmentByTag(makeDialogFragmentTag(-1, id)) != null) {
            //id相同的不能多次显示
            return;
        }
        dialogFragment.setTarget(null, id);
        dialogFragment.show(mFragmentManager, DIALOG_FRAGMENT_TAG + id);
    }

    protected void dismissDialogFragment(int id) {
        BaseDialogFragment dialogFragment = (BaseDialogFragment) mFragmentManager
                .findFragmentByTag(makeDialogFragmentTag(-1, id));
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
    }

    @Override
    public void onDialogClick(BaseDialogFragment dialog, int which) {
    }

    @Override
    public void onDialogCancel(BaseDialogFragment dialog) {
    }

    @Override
    public void onDialogDismiss(BaseDialogFragment dialog) {
    }

    @Override
    public void onDialogShow(BaseDialogFragment dialog) {
    }

    protected int getFragmentContainerId() {
        return -1;
    }

    public void startFragment(int containerViewId, BaseFragment fragment) {
        int backId = mFragmentManager.beginTransaction().add(containerViewId, fragment)
                .addToBackStack(null).commit();
        fragment.setBackStackId(backId);
    }

    public void startFragment(Intent intent) {
        int containerViewId = getFragmentContainerId();
        if (containerViewId <= 0) {
            containerViewId = android.R.id.content;
        }
        BaseFragment.startFragmentForResult(this, containerViewId, intent, null, -1);
    }

    /* package */void pushFragment(BaseFragment fragment) {
        mFragmentStack.push(fragment);
    }

    /* package */void removeFragment(BaseFragment fragment) {
        mFragmentStack.remove(fragment);
    }

    /**
     * 获取指定Fragment的上一个Fragment
     * 
     * @param fragment 指定的Fragment
     * @return 上一个Fragment，如果指定的Fragment不为null；否则返回最后一个Fragment
     */
    /* package */BaseFragment getPreviousFragment(BaseFragment fragment) {
        if (mFragmentStack.isEmpty()) {
            return null;
        }
        if (fragment == null) {
            return mFragmentStack.lastElement();
        }
        ListIterator<BaseFragment> li = mFragmentStack.listIterator(mFragmentStack.size());
        while (li.hasPrevious()) {
            if (li.previous() == fragment) {
                if (li.hasPrevious()) {
                    return li.previous();
                }
            }
        }
        return null;
    }

    /* package */BaseFragment[] getStackedFragments() {
        BaseFragment[] fragments = new BaseFragment[mFragmentStack.size()];
        mFragmentStack.toArray(fragments);
        return fragments;
    }

    /* package */boolean isFragmentOnTop(BaseFragment fragment) {
        return !mFragmentStack.isEmpty() && mFragmentStack.lastElement() == fragment;
    }

    protected boolean isStatisticsAgentEnabled() {
        return true;
    }

    /* package */void markPageStart(String page) {
        if (!isStatisticsAgentEnabled()) {
            return;
        }
        ((BaseApplication) getApplication()).getStatisticsAgent().markPageStart(page);
    }

    /* package */void markPageEnd(String page) {
        if (!isStatisticsAgentEnabled()) {
            return;
        }
        ((BaseApplication) getApplication()).getStatisticsAgent().markPageEnd(page);
    }

    public void markEvent(String event) {
        if (!isStatisticsAgentEnabled()) {
            return;
        }
        ((BaseApplication) getApplication()).getStatisticsAgent().markEvent(event);
    }

    public void markEvent(String event, String type) {
        if (!isStatisticsAgentEnabled()) {
            return;
        }
        ((BaseApplication) getApplication()).getStatisticsAgent().markEvent(event, type);
    }

    public void markEventBegin(String event) {
        if (!isStatisticsAgentEnabled()) {
            return;
        }
        ((BaseApplication) getApplication()).getStatisticsAgent().markEventBegin(event);
    }

    public void markEventEnd(String event) {
        if (!isStatisticsAgentEnabled()) {
            return;
        }
        ((BaseApplication) getApplication()).getStatisticsAgent().markEventEnd(event);
    }

    public void markAttribute(String name, long value) {
        if (!isStatisticsAgentEnabled()) {
            return;
        }
        ((BaseApplication) getApplication()).getStatisticsAgent().markAttribute(name, value);
    }

}
