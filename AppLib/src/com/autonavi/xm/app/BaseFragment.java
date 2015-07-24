
package com.autonavi.xm.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.autonavi.xm.content.SharedIntent;
import com.autonavi.xm.view.BaseScreen;

public class BaseFragment extends android.support.v4.app.Fragment implements BaseFragmentCallback,
        BaseDialogFragmentCallback {

    public static final int FLAG_SINGLE_TOP = 0x1;

    public static final int FLAG_CLEAR_TOP = 0x2;

    public static final int RESULT_OK = android.app.Activity.RESULT_OK;

    public static final int RESULT_CANCELED = android.app.Activity.RESULT_CANCELED;

    public static final int RESULT_FIRST_USER = android.app.Activity.RESULT_FIRST_USER;

    public static final int BIND_AUTO_CREATE = android.app.Activity.BIND_AUTO_CREATE;

    private static final String SAVE_BACK_STACK_ID = "autonavi:save:back_stack_id";

    private static final String SAVE_RESULT_CODE = "autonavi:save:result_code";

    private static final String SAVE_RESULT_DATA = "autonavi:save:result_data";

    private static final String SAVE_IS_TOUCH_EVENT_PASSED = "autonavi:save:is_touch_event_passed";

    private static final String SAVE_IS_USER_INTERACTABLE = "autonavi:save:is_user_interactable";

    private static final String SAVE_IS_USER_INTERACTION_FROZEN = "autonavi:save:is_user_interaction_frozen";

    private static final String ARGUMENT_START_FRAGMENT_FOR_RESULT = "autonavi:argument:start_fragment_for_result";

    private BaseActivity mBaseActivity;

    private FragmentManager mFragmentManager;

    private int mContainerResId;

    private int mBackStackId = -1;

    private boolean mIsOnStartViewCalled = false;

    private boolean mIsOnResumeViewCalled = false;

    private int mResultCode = Activity.RESULT_CANCELED;

    private Intent mResultData;

    private CharSequence mTitle;

    private View mMainView;

    private BaseScreen mBaseScreen;

    private ViewGroup mContainer;

    private boolean mIsTouchEventPassed = false;

    private boolean mIsUserInteractable = true;

    private boolean mIsUserInteractionFrozen = false;

    /**
     * 是否启用ScreenView，一个通用的界面框架。如果启用则 content view 会被置入 ScreenView ，View层级多一层。
     * 
     * @return true为启动，否则为禁用
     */
    protected boolean isScreenViewEnabled() {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBaseActivity = (BaseActivity) getActivity();
        mFragmentManager = getFragmentManager();

        if (savedInstanceState != null) {
            mBackStackId = savedInstanceState.getInt(SAVE_BACK_STACK_ID, mBackStackId);
            mResultCode = savedInstanceState.getInt(SAVE_RESULT_CODE, mResultCode);
            mResultData = savedInstanceState.getParcelable(SAVE_RESULT_DATA);
            mIsTouchEventPassed = savedInstanceState.getBoolean(SAVE_IS_TOUCH_EVENT_PASSED,
                    mIsTouchEventPassed);
            mIsUserInteractable = savedInstanceState.getBoolean(SAVE_IS_USER_INTERACTABLE,
                    mIsUserInteractable);
            mIsUserInteractionFrozen = savedInstanceState.getBoolean(
                    SAVE_IS_USER_INTERACTION_FROZEN, mIsUserInteractionFrozen);
        }

        BaseFragment last = mBaseActivity.getPreviousFragment(null);
        if (last != null) {
            last.onPauseView();
        }
        mBaseActivity.pushFragment(this);
    }

    protected void onNewIntent(Intent intent) {
    }

    /**
     * set content view here
     */
    protected void onCreateView(Bundle savedInstanceState) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        mContainerResId = container.getId();

        if (isScreenViewEnabled()) {
            mBaseScreen = new BaseScreen(mBaseActivity);
            mMainView = mBaseScreen.getScreenView();
        } else {
            mContainer = container;
        }
        onCreateView(savedInstanceState);
        return mMainView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //在 onViewCreated 之前无法生效，因此在此处再次调用
        setTouchEventPassed(mIsTouchEventPassed);
        setUserInteractable(mIsUserInteractable);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_BACK_STACK_ID, mBackStackId);
        outState.putInt(SAVE_RESULT_CODE, mResultCode);
        if (mResultData != null) {
            outState.putParcelable(SAVE_RESULT_DATA, mResultData);
        }
        outState.putBoolean(SAVE_IS_TOUCH_EVENT_PASSED, mIsTouchEventPassed);
        outState.putBoolean(SAVE_IS_USER_INTERACTABLE, mIsUserInteractable);
        outState.putBoolean(SAVE_IS_USER_INTERACTION_FROZEN, mIsUserInteractionFrozen);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mIsOnStartViewCalled) {
            mIsOnStartViewCalled = true;
            onStartView();
        }

        markPageStart(getClass().getSimpleName());
    }

    @Override
    public void onStartView() {
        if (isHidden()) {
            setVisible(true);
            mFragmentManager.beginTransaction().show(this).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mIsOnResumeViewCalled) {
            mIsOnResumeViewCalled = true;
            onResumeView();
            BaseFragment prev = mBaseActivity.getPreviousFragment(this);
            if (prev != null) {
                prev.onStopView();
            }
        }
    }

    @Override
    public void onResumeView() {
        if (mIsUserInteractionFrozen) {
            setUserInteractionFrozen(false);
        }
    }

    @Override
    public void onPauseView() {
    }

    @Override
    public void onPause() {
        if (isRemoving()) {
            checkFragmentResult();

            BaseFragment prev = mBaseActivity.getPreviousFragment(this);
            if (prev != null) {
                prev.onStartView();
            }
            onPauseView();
        }
        markPageEnd(getClass().getSimpleName());
        super.onPause();
    }

    @Override
    public void onStopView() {
        if (!isHidden() && !isRemoving()) {
            mFragmentManager.beginTransaction().hide(this).commit();
        }
    }

    @Override
    public void onStop() {
        if (isRemoving()) {
            onStopView();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mBaseActivity.removeFragment(this);
        BaseFragment last = mBaseActivity.getPreviousFragment(null);
        if (last != null) {
            last.onResumeView();
        }
        super.onDestroy();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    /* package */void setBackStackId(int id) {
        if (mBackStackId >= 0) {
            return;
        }
        mBackStackId = id;
    }

    protected boolean isOnTop() {
        return mBaseActivity.isFragmentOnTop(this);
    }

    private void setVisible(boolean visible) {
        View view = getView();
        if (view != null) {
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void replaceContentView(View contentView) {
        ViewGroup fragView = (ViewGroup) getView();
        if (fragView != null) {
            fragView.removeView(mMainView);
            LayoutParams contentParams = contentView.getLayoutParams();
            contentView.setLayoutParams(fragView.getLayoutParams());
            if (contentParams != null) {
                fragView.setLayoutParams(contentParams);
            }
            fragView.addView(contentView);
        }
    }

    protected void setContentView(int layoutResId) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setContentView(layoutResId);
        } else {
            View contentView = mBaseActivity.getLayoutInflater().inflate(layoutResId, mContainer,
                    false);
            if (mMainView == null) {
                mMainView = contentView;
            } else {
                replaceContentView(contentView);
            }
        }
    }

    protected void setContentView(View view) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setContentView(view);
        } else {
            if (mMainView == null) {
                mMainView = view;
            } else {
                replaceContentView(view);
            }
        }
    }

    protected void setContentView(View view, LayoutParams params) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setContentView(view, params);
        } else {
            view.setLayoutParams(params);
            if (mMainView == null) {
                mMainView = view;
            } else {
                replaceContentView(view);
            }
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param notitle
     */
    protected void setNoTitle(boolean notitle) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setNoTitle(notitle);
        }
    }

    protected void setTitle(CharSequence title) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitle(title);
        } else {
            mTitle = title;
        }
    }

    protected void setTitle(int titleId) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitle(titleId);
        } else {
            mTitle = getResources().getText(titleId);
        }
    }

    protected CharSequence getTitle() {
        if (isScreenViewEnabled()) {
            return mBaseScreen.getTitle();
        }
        return mTitle;
    }

    protected void setTitleIcon(int resId) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleIcon(resId);
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param simple
     */
    protected void setTitleSimple(boolean simple) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleSimple(simple);
        }
    }

    /**
     * 设置TitleBar的背景
     * 
     * @param background
     */
    protected void setTitleBackground(Drawable background) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleBackground(background);
        }
    }

    /**
     * 设置TitleBar的背景
     * 
     * @param resId
     */
    protected void setTitleBackground(int resId) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleBackground(resId);
        }
    }

    /**
     * 设置TitleBar的背景颜色
     * 
     * @param color
     */
    protected void setTitleBackgroundColor(int color) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleBackgroundColor(color);
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param layoutResId
     */
    protected void setTitleContent(int layoutResId) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleContent(layoutResId);
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param view
     */
    protected void setTitleContent(View view) {
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
    protected void setTitleContent(View view, ViewGroup.LayoutParams params) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleContent(view, params);
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param layoutResId
     */
    protected void setTitleRightContent(int layoutResId) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleRightContent(layoutResId);
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param view
     */
    protected void setTitleRightContent(View view) {
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
    protected void setTitleRightContent(View view, ViewGroup.LayoutParams params) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleRightContent(view, params);
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param layoutResId
     */
    protected void setTitleLeftContent(int layoutResId) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleLeftContent(layoutResId);
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param view
     */
    protected void setTitleLeftContent(View view) {
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
    protected void setTitleLeftContent(View view, ViewGroup.LayoutParams params) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setTitleLeftContent(view, params);
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param background
     */
    protected void setContentBackground(Drawable background) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setContentBackground(background);
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param resId
     */
    protected void setContentBackground(int resId) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setContentBackground(resId);
        }
    }

    /**
     * ScreenView 启用时才有效
     * 
     * @param color
     */
    protected void setContentBackgroundColor(int color) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setContentBackgroundColor(color);
        }
    }

    /**
     * 设置Fragment背景
     * 
     * @param background
     */
    @SuppressWarnings("deprecation")
    public void setBackground(Drawable background) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setBackground(background);
        } else {
            View view = getView();
            if (view != null) {
                view.setBackgroundDrawable(background);
            }
        }
    }

    /**
     * 设置Fragment背景
     * 
     * @param resId
     */
    public void setBackground(int resId) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setBackground(resId);
        } else {
            View view = getView();
            if (view != null) {
                view.setBackgroundResource(resId);
            }
        }
    }

    /**
     * 设置Fragment背景
     * 
     * @param color
     */
    public void setBackgroundColor(int color) {
        if (isScreenViewEnabled()) {
            mBaseScreen.setBackgroundColor(color);
        } else {
            View view = getView();
            if (view != null) {
                view.setBackgroundColor(color);
            }
        }
    }

    protected View findViewById(int id) {
        return mMainView.findViewById(id);
    }

    /**
     * 设置是否将触摸事件传递到下层View
     * 
     * @param passed true则传递到下层，否则不传递
     */
    protected void setTouchEventPassed(boolean passed) {
        mIsTouchEventPassed = passed;
        View view = getView();
        if (view != null) {
            if (!passed) {
                //采用setClickable方式，在4.0以上系统会造成子View状态跟随改变，因此采用setOnTouchListener方式
                view.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
            } else {
                view.setOnTouchListener(null);
            }
        }
    }

    /**
     * 设置该Fragment是否可交互
     * 
     * @param interactable true则可交互，否则不可交互
     */
    protected void setUserInteractable(boolean interactable) {
        mIsUserInteractable = interactable;
        setUserInteractableInternal(interactable);
    }

    protected boolean isUserInteractable() {
        return !mIsUserInteractionFrozen && mIsUserInteractable;
    }

    private void setUserInteractableInternal(boolean interactable) {
        View view = getView();
        if (view == null) {
            return;
        }
    }

    private void checkFragmentResult() {
        Bundle arguments = getArguments();
        if (!(arguments != null && arguments.getBoolean(ARGUMENT_START_FRAGMENT_FOR_RESULT, false))) {
            return;
        }
        Fragment target = getTargetFragment();
        int requestCode = getTargetRequestCode();
        if (target != null && requestCode >= 0) {
            int resultCode;
            Intent resultData;
            synchronized (this) {
                resultCode = mResultCode;
                resultData = mResultData;
            }
            target.onActivityResult(requestCode, resultCode, resultData);
        }
    }

    /**
     * 冻结/解冻人机交互，一般会在onResumeView时解冻。 如果本来就被设置为不可交互（{@link #mIsUserInteractable}
     * 为false）则该方法无效
     * 
     * @param isFrozen true为冻结，否则解冻
     */
    /* package */void setUserInteractionFrozen(boolean isFrozen) {
        //TODO 因为click事件有post，该方法无法完全拦截click事件，寻找更有效的方法...
        if (!mIsUserInteractable) {
            return;
        }
        if (!isFrozen) {
            pressUpAllViews((ViewGroup) mMainView);
        }
        mIsUserInteractionFrozen = isFrozen;
        setUserInteractableInternal(!isFrozen);
    }

    /**
     * 解除所有View的pressed状态（递归）。冻结触摸事件后，View只接收到down事件，就会卡在pressed状态，通过该方法解除。
     * 
     * @param viewGroup 要解除状态的ViewGroup
     */
    private void pressUpAllViews(ViewGroup viewGroup) {
        if (viewGroup == null) {
            return;
        }
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = viewGroup.getChildAt(i);
            if (child.isPressed()) {
                child.setPressed(false);
            }
            if (child instanceof ViewGroup) {
                pressUpAllViews((ViewGroup) child);
            }
        }
    }

    /**
     * 启动一个 Fragment
     * 
     * @param intent 启动的Intent
     */
    public void startFragment(Intent intent) {
        startFragmentForResult(intent, -1);
    }

    /**
     * 启动一个想获取结果的Fragment。当那个Fragment退出时 onActivityResult()
     * 方法将被调用。requestCode为负数时等同与调用 {@link #startFragment(Intent)}
     * 
     * @param intent 启动的Intent
     * @param requestCode 为非负数时会通过 onActivityResult 返回结果
     */
    public void startFragmentForResult(Intent intent, int requestCode) {
        setUserInteractionFrozen(true);
        startFragmentForResult(mBaseActivity, mContainerResId, intent, this, requestCode);
    }

    /* package */static void startFragmentForResult(BaseActivity activity, int containerViewId,
            Intent intent, BaseFragment target, int requestCode) {
        try {
            FragmentManager manager = activity.getSupportFragmentManager();
            int flags = intent.getFlags();
            Class<?> clazz = Class.forName(intent.getComponent().getClassName());
            BaseFragment fragment = null;
            if (flags > 0) {
                if ((flags & FLAG_SINGLE_TOP) != 0) {
                    BaseFragment frag = activity.getPreviousFragment(null);
                    if (frag != null && frag instanceof BaseFragment && clazz.isInstance(frag)) {
                        fragment = frag;
                    }
                }
                if ((flags & FLAG_CLEAR_TOP) != 0) {
                    Fragment[] fragments = activity.getStackedFragments();
                    if (fragments != null) {
                        for (int i = fragments.length - 1; i >= 0; i--) {
                            Fragment frag = fragments[i];
                            if (frag instanceof BaseFragment && clazz.isInstance(frag)) {
                                fragment = (BaseFragment) frag;
                                break;
                            }
                        }
                    }
                    if (fragment != null && fragment.mBackStackId >= 0) {
                        manager.popBackStack(fragment.mBackStackId, 0);
                    } else {//找不倒backId，默认取最底层的Fragment
                        manager.popBackStack();
                    }
                }
            }
            if (fragment == null) {
                fragment = (BaseFragment) clazz.newInstance();
                Bundle extras = intent.getExtras();
                Fragment.SavedState savedState = null;
                if (extras != null) {
                    savedState = extras.getParcelable(SharedIntent.EXTRA_SAVED_INSTANCE_STATE);
                    if (savedState != null) {
                        fragment.setInitialSavedState(savedState);
                        extras.remove(SharedIntent.EXTRA_SAVED_INSTANCE_STATE);
                    }
                }
                if (requestCode >= 0) {
                    if (extras == null) {
                        extras = new Bundle();
                    }
                    extras.putBoolean(ARGUMENT_START_FRAGMENT_FOR_RESULT, true);
                    fragment.setTargetFragment(target, requestCode);
                }
                fragment.setArguments(extras);
                //FIXME AllowingStateLoss可以避免Activity在后台时的调用异常，但这将引发系列问题，需要重新考虑
                fragment.mBackStackId = manager.beginTransaction().add(containerViewId, fragment)
                        .addToBackStack(null).commitAllowingStateLoss();
            } else {
                fragment.onNewIntent(intent);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (java.lang.InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置结果，用于回传给调用者
     * 
     * @param resultCode 结果码
     */
    public void setResult(int resultCode) {
        setResult(resultCode, null);
    }

    /**
     * 设置结果，用于回传给调用者
     * 
     * @param resultCode 结果码
     * @param data 结果数据
     */
    public void setResult(int resultCode, Intent data) {
        synchronized (this) {
            mResultCode = resultCode;
            mResultData = data;
        }
    }

    /**
     * 退出此Fragment，栈中该Fragment上面的也会退出
     */
    public void popBack() {
        popBack(false);
    }

    public void popBackImmediate() {
        popBack(true);
    }

    private void popBack(boolean immediate) {
        if (mBackStackId >= 0) {
            if (immediate) {
                mFragmentManager.popBackStackImmediate(mBackStackId,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } else {
                mFragmentManager.popBackStack(mBackStackId,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            mBackStackId = -1;
        } else {
            if (immediate) {
                mFragmentManager.popBackStackImmediate();
            } else {
                mFragmentManager.popBackStack();
            }
        }
    }

    protected ComponentName startService(Intent service) {
        return mBaseActivity.startService(service);
    }

    protected boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return mBaseActivity.bindService(service, conn, flags);
    }

    protected void unbindService(ServiceConnection conn) {
        mBaseActivity.unbindService(conn);
    }

    protected SharedPreferences getSharedPreferences(String name, int mode) {
        return mBaseActivity.getSharedPreferences(name, mode);
    }

    protected SharedPreferences getPreferences(int mode) {
        return mBaseActivity.getPreferences(mode);
    }

    protected void runOnUiThread(Runnable action) {
        mBaseActivity.runOnUiThread(action);
    }

    private String getDialogFragmentTag(int id) {
        return BaseActivity.makeDialogFragmentTag(mBackStackId, id);
    }

    protected void showDialog(BaseDialogFragment dialogFragment, int id) {
        String tag = getDialogFragmentTag(id);
        if (mFragmentManager.findFragmentByTag(tag) != null) {
            //id相同的不能多次显示
            return;
        }
        setUserInteractionFrozen(true);
        dialogFragment.setTarget(this, id);
        dialogFragment.show(mFragmentManager.beginTransaction(), tag);
    }

    protected void dismissDialog(int id) {
        BaseDialogFragment dialogFragment = (BaseDialogFragment) mFragmentManager
                .findFragmentByTag(getDialogFragmentTag(id));
        if (dialogFragment != null) {
            //FIXME AllowingStateLoss可以避免Activity在后台时的调用异常，但这将引发系列问题，需要重新考虑
            dialogFragment.dismissAllowingStateLoss();
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

    public void showToast(int textId) {
        mBaseActivity.showToast(textId);
    }

    public void showToast(String text) {
        mBaseActivity.showToast(text);
    }

    public void showToast(int textId, int iconId) {
        mBaseActivity.showToast(textId, iconId);
    }

    public void cancelToast() {
        mBaseActivity.cancelToast();
    }

    private void markPageStart(String page) {
        ((BaseActivity) getActivity()).markPageStart(page);
    }

    private void markPageEnd(String page) {
        ((BaseActivity) getActivity()).markPageEnd(page);
    }

    public void markEvent(String event) {
        ((BaseActivity) getActivity()).markEvent(event);
    }

    public void markEvent(String event, String type) {
        ((BaseActivity) getActivity()).markEvent(event, type);
    }

    public void markEventBegin(String event) {
        ((BaseActivity) getActivity()).markEventBegin(event);
    }

    public void markEventEnd(String event) {
        ((BaseActivity) getActivity()).markEventEnd(event);
    }

    public void markAttribute(String name, long value) {
        ((BaseActivity) getActivity()).markAttribute(name, value);
    }

}
