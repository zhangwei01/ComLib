
package com.autonavi.xm.app;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class BaseDialogFragment extends android.support.v4.app.DialogFragment {

    private static final String SAVED_DIALOG_ID = "autonavi:dialogId";

    private int mDialogId = -1;

    /* package */void setTarget(Fragment target, int dialogId) {
        super.setTargetFragment(target, dialogId);
        mDialogId = dialogId;
    }

    public int getDialogId() {
        return mDialogId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mDialogId = savedInstanceState.getInt(SAVED_DIALOG_ID, mDialogId);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        BaseDialogFragmentCallback callback = getCallback();
        if (callback != null) {
            callback.onDialogShow(this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mDialogId != -1) {
            outState.putInt(SAVED_DIALOG_ID, mDialogId);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        BaseDialogFragmentCallback callback = getCallback();
        if (callback != null) {
            callback.onDialogCancel(this);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Fragment target = getTargetFragment();
        if (target instanceof BaseFragment) {
            ((BaseFragment) target).setUserInteractionFrozen(false);
        }
        BaseDialogFragmentCallback callback = getCallback();
        if (callback != null) {
            callback.onDialogDismiss(this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Fragment target = getTargetFragment();
        if (target instanceof BaseFragment) {
            ((BaseFragment) target).setUserInteractionFrozen(false);
        }
    }

    protected void onDialogClick(int which) {
        BaseDialogFragmentCallback callback = getCallback();
        if (callback != null) {
            callback.onDialogClick(this, which);
        }
    }

    private BaseDialogFragmentCallback getCallback() {
        //target fragment优先，activity之后
        Fragment target = getTargetFragment();
        if (target != null) {
            if (target instanceof BaseDialogFragmentCallback) {
                return (BaseDialogFragmentCallback) target;
            }
        }
        FragmentActivity activity = getActivity();
        if (activity instanceof BaseDialogFragmentCallback) {
            return (BaseDialogFragmentCallback) activity;
        }
        return null;
    }

    /**
     * This method was disabled.
     */
    @Deprecated
    @Override
    public final void setTargetFragment(Fragment fragment, int requestCode) {
        // use setTarget instead
    }
}
