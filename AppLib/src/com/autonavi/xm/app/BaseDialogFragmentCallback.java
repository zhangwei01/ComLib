
package com.autonavi.xm.app;

/* package */interface BaseDialogFragmentCallback {

    public void onDialogClick(BaseDialogFragment dialog, int which);

    public void onDialogCancel(BaseDialogFragment dialog);

    public void onDialogDismiss(BaseDialogFragment dialog);

    public void onDialogShow(BaseDialogFragment dialog);
}
