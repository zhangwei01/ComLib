
package com.autonavi.xm.widget;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 自动开启跑马灯效果的TextView，无需获取焦点。(为达到跑马灯效果，已强制设为单行)
 * 
 * @author junbin.lin
 */
public class MarqueeTextView extends TextView {

    public MarqueeTextView(Context context) {
        this(context, null, 0);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setSingleLine();
        setEllipsize(TruncateAt.MARQUEE);
    }

    /**
     * 通过返回true，使得MarqueTextView默认开启跑马灯效果，无需手动点击该TextView。
     */
    @Override
    public boolean isFocused() {
        return true;
    }

}
