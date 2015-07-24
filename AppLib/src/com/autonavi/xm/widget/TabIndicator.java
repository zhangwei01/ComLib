
package com.autonavi.xm.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.autonavi.xm.app.lib.R;

public class TabIndicator extends FrameLayout {

    public static final int STYLE_LEFT = 0;

    public static final int STYLE_MIDDLE = 1;

    public static final int STYLE_RIGHT = 2;

    private final TextView mTextLabel;

    private final ImageView mImageIcon;

    public TabIndicator(Context context) {
        this(context, null, 0);
    }

    public TabIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setBackgroundResource(R.drawable.btn_default_selector);
        View.inflate(context, R.layout.tab_indicator, this);
        mTextLabel = (TextView) findViewById(R.id.tab_label);
        mImageIcon = (ImageView) findViewById(R.id.tab_icon);
    }

    public TabIndicator setLabel(CharSequence label) {
        mTextLabel.setText(label);
        return this;
    }

    public TabIndicator setLabel(int resId) {
        mTextLabel.setText(resId);
        return this;
    }

    public TabIndicator setIcon(Drawable icon) {
        mImageIcon.setImageDrawable(icon);
        return this;
    }

    public TabIndicator setIcon(int resId) {
        mImageIcon.setImageResource(resId);
        return this;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        mTextLabel.setVisibility(!isSelected() ? VISIBLE : GONE);
        mImageIcon.setVisibility(isSelected() ? VISIBLE : GONE);
    }
}
