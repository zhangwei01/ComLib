
package com.autonavi.xm.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.autonavi.xm.app.lib.R;

public class BaseToast {

    private static final int WHAT_REFRESH_TOAST = 0;

    private final Handler mMainHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case WHAT_REFRESH_TOAST:
                    if (mToast == null) {
                        mToast = new Toast(mContext);
                        mToast.setView(View.inflate(mContext, R.layout.base_toast, null));
                        //                        mToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0);
                        //                        mToast.setMargin(0, 100);
                        mTxt = (TextView) (mToast.getView().findViewById(R.id.text));
                        mImg = (ImageView) (mToast.getView().findViewById(R.id.icon));
                    }
                    Object[] args = (Object[]) msg.obj;
                    mTxt.setText((String) args[0]);
                    mImg.setVisibility((Integer) args[1] <= 0 ? View.GONE : View.VISIBLE);
                    mImg.setImageResource((Integer) args[1]);
                    mToast.setDuration((Integer) args[2]);
                    mToast.show();
                    break;
                default:
                    break;
            }
        };

    };

    private Toast mToast;

    private TextView mTxt;

    private ImageView mImg;

    private final Context mContext;

    public BaseToast(Context context) {
        mContext = context;
    }

    public void cancel() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    public void show(int textId, int iconId, int duration) {
        show(mContext.getString(textId), iconId, duration);
    }

    public void show(int textId, int iconId) {
        show(mContext.getString(textId), iconId, Toast.LENGTH_SHORT);
    }

    public void show(int textId) {
        show(mContext.getString(textId), 0, Toast.LENGTH_SHORT);
    }

    public void show(String text, int iconId, int duration) {
        mMainHandler.obtainMessage(WHAT_REFRESH_TOAST, new Object[] {
                text, iconId, duration
        }).sendToTarget();
    }

    public void show(String text, int iconId) {
        show(text, iconId, Toast.LENGTH_SHORT);
    }

    public void show(String text) {
        show(text, 0, Toast.LENGTH_SHORT);
    }

}
