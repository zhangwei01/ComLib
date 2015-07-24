
package com.autonavi.xm.widget;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.autonavi.xm.app.lib.R;

public class CompoundEditText extends FrameLayout implements View.OnClickListener {

    private final AutoCompleteTextView mAutoEditText;

    private final TextView mExtraText;

    public CompoundEditText(Context context) {
        this(context, null, 0);
    }

    public CompoundEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CompoundEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        View.inflate(context, R.layout.compound_edit_text, this);
        mAutoEditText = (AutoCompleteTextView) findViewById(R.id.edit_text);
        mExtraText = (TextView) findViewById(R.id.extra_text);
        findViewById(R.id.dropDown).setOnClickListener(this);
    }

    public AutoCompleteTextView getAutoCompleteTextView() {
        return mAutoEditText;
    }

    public void setText(int resid) {
        mAutoEditText.setText(resid);
    }

    public void setText(CharSequence text) {
        mAutoEditText.setText(text);
    }

    public Editable getText() {
        return mAutoEditText.getText();
    }

    public void setHint(int resid) {
        mAutoEditText.setHint(resid);
    }

    public void setHint(CharSequence hint) {
        mAutoEditText.setHint(hint);
    }

    public void setImeOptions(int imeOptions) {
        mAutoEditText.setImeOptions(imeOptions);
    }

    public void setOnEditorActionListener(OnEditorActionListener listener) {
        mAutoEditText.setOnEditorActionListener(listener);
    }

    public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
        mAutoEditText.setAdapter(adapter);
    }

    public void setExtraText(int resid) {
        mExtraText.setText(resid);
    }

    public void setExtraText(CharSequence text) {
        mExtraText.setText(text);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.dropDown) {
            mAutoEditText.showDropDown();
        }
    }

}
