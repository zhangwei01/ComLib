
package com.autonavi.xm.text;

import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;

import java.util.Arrays;

/**
 * 接受指定字符的输入过滤器
 * 
 * @author i.F
 * @since 2012-7-28
 */
public class AcceptableInputFilter implements InputFilter {

    /**
     * 可接受字母
     */
    public static final int ACCEPT_LETTER = 0x1;

    /**
     * 可接受数字
     */
    public static final int ACCEPT_DIGIT = 0x2;

    /**
     * 可接受空白字符
     */
    public static final int ACCEPT_WHITE_SPACE = 0x4;

    private int mAccept = 0;

    private char[] mAcceptChars;

    private OnRejectListener mOnRejectListener;

    /**
     * 指定接受的字符类型创建实例
     * 
     * @param accept 接受的字符类型
     */
    public AcceptableInputFilter(int accept) {
        mAccept = accept;
    }

    /**
     * 指定接受的所有字符创建实例
     * 
     * @param chars 接受的所有字符
     */
    public AcceptableInputFilter(char[] chars) {
        mAcceptChars = new char[chars.length];
        System.arraycopy(chars, 0, mAcceptChars, 0, chars.length);
        Arrays.sort(mAcceptChars);
    }

    /**
     * 设置输入字符不被接受的回调
     * 
     * @param listener 回调对象
     */
    public void setOnRejectListener(OnRejectListener listener) {
        mOnRejectListener = listener;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart,
            int dend) {
        char[] v = new char[end - start];
        TextUtils.getChars(source, start, end, v, 0);
        String s = new String(v).trim();

        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
            if (!isAccepted(c)) {
                if (mOnRejectListener != null) {
                    mOnRejectListener.onRejected(this, c);
                }
                return "";
            }
        }

        if (source instanceof Spanned) {
            SpannableString sp = new SpannableString(s);
            TextUtils.copySpansFrom((Spanned) source, start, end, null, sp, 0);
            return sp;
        } else {
            return s;
        }
    }

    private boolean isAccepted(char c) {
        if (mAccept > 0) {
            if ((mAccept & ACCEPT_LETTER) != 0) {
                if (isLetter(c)) {
                    return true;
                }
            }
            if ((mAccept & ACCEPT_DIGIT) != 0) {
                if (Character.isDigit(c)) {
                    return true;
                }
            }
            if ((mAccept & ACCEPT_WHITE_SPACE) != 0) {
                if (Character.isWhitespace(c)) {
                    return true;
                }
            }
        } else if (mAcceptChars != null && mAcceptChars.length > 0) {
            return Arrays.binarySearch(mAcceptChars, c) >= 0;
        }
        return false;
    }

    private static boolean isLetter(int codePoint) {
        return (('A' <= codePoint && codePoint <= 'Z') || ('a' <= codePoint && codePoint <= 'z'));
    }

    /**
     * 输入字符不被接受的回调
     * 
     * @author i.F
     * @since 2012-7-28
     */
    public static interface OnRejectListener {

        /**
         * 当输入字符不被接受时被调用
         * 
         * @param filter AcceptInputFilter实例对象
         * @param c 不被接受的字符
         */
        public void onRejected(AcceptableInputFilter filter, char c);

    }

}
