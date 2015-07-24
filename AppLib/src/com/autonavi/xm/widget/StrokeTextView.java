
package com.autonavi.xm.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.autonavi.xm.app.lib.R;

/**
 * 文字带描边的TextView
 * 
 * @author i.F
 * @see #setBorderWidth(int)
 * @see #setBorderColor(int)
 * @see #setBorderColor(ColorStateList)
 */
public class StrokeTextView extends TextView {

    /**
     * 描边宽度，为0则不描边
     */
    private int mBorderWidth;

    /**
     * 描边颜色，为null则不描边
     */
    private ColorStateList mBorderColor;

    /**
     * @see TextView#TextView(Context)
     */
    public StrokeTextView(Context context) {
        super(context);
        onCreate(context, null, 0);
    }

    /**
     * @see TextView#TextView(Context, AttributeSet)
     */
    public StrokeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        onCreate(context, attrs, 0);
    }

    /**
     * @see TextView#TextView(Context, AttributeSet,int)
     */
    public StrokeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        onCreate(context, attrs, defStyle);
    }

    private void onCreate(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StrokeTextView, defStyle,
                0);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attrIdx = a.getIndex(i);
            switch (attrIdx) {
            //case R.styleable.StrokeTextView_borderColor: 
                case 1: {
                    setBorderColor(a.getColorStateList(attrIdx));
                    break;
                }
                //case R.styleable.StrokeTextView_borderWidth: 
                case 0: {
                    setBorderWidth(a.getDimensionPixelSize(attrIdx, 0));
                    break;
                }
            }
        }
        a.recycle();
    }

    /**
     * 获取描边宽度
     * 
     * @return 描边宽度
     */
    public int getBorderWidth() {
        return mBorderWidth;
    }

    /**
     * 设置描边宽度
     * 
     * @param borderWidth 描边宽度
     */
    public void setBorderWidth(int borderWidth) {
        mBorderWidth = borderWidth;
    }

    /**
     * 获取描边颜色
     * 
     * @return 描边颜色
     */
    public ColorStateList getBorderColors() {
        return mBorderColor;
    }

    /**
     * 设置描边颜色
     * 
     * @param color 描边颜色
     */
    public void setBorderColor(int color) {
        mBorderColor = ColorStateList.valueOf(color);
    }

    /**
     * 设置描边颜色
     * 
     * @param colors 描边颜色
     */
    public void setBorderColor(ColorStateList colors) {
        if (colors == null) {
            throw new NullPointerException();
        }
        mBorderColor = colors;
    }

    private boolean mIgnoreInvalidate = false;

    @Override
    public void invalidate() {
        if (mIgnoreInvalidate) {
            return;
        }
        super.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /*
         * 调用两次super.onDraw(canvas)实现描边效果，第一次为绘制描边底色
         * !!!调用 setTextColor 会触发 invalidate ，造成无限循环重绘，因此增加 mIgnoreInvalidate
         */
        if (mBorderWidth > 0 && mBorderColor != null) {
            TextPaint textPaint = getPaint();
            ColorStateList textColor = getTextColors();
            mIgnoreInvalidate = true;
            setTextColor(mBorderColor);
            textPaint.setStyle(Paint.Style.STROKE);
            textPaint.setStrokeWidth(mBorderWidth);
            mIgnoreInvalidate = false;
            super.onDraw(canvas);
            mIgnoreInvalidate = true;
            setTextColor(textColor);
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setStrokeWidth(1);
            mIgnoreInvalidate = false;
        }
        super.onDraw(canvas);
    }

}
