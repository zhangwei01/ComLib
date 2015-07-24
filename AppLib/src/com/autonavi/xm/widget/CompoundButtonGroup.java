
package com.autonavi.xm.widget;

import android.widget.CompoundButton;

import java.util.Enumeration;
import java.util.Vector;

public class CompoundButtonGroup {

    private Vector<CompoundButton> mButtons;

    private OnCheckedChangeListener mOnCheckedChangeListener;

    private int mCheckedId = -1;

    public CompoundButtonGroup() {
        mButtons = new Vector<CompoundButton>();
    }

    private void setCheckedId(CompoundButton button) {
        int id = button != null ? button.getId() : -1;
        int preId = mCheckedId;
        mCheckedId = id;
        if (preId != id && mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener.onCheckedChanged(this, button);
        }
    }

    public void add(CompoundButton button) {
        mButtons.add(button);
        button.setOnCheckedChangeListener(mMemberCheckedChangeListener);
        if (button.isChecked()) {
            if (mCheckedId == -1) {
                setCheckedId(button);
            } else {
                button.setChecked(false);
            }
        }
    }

    public void remove(CompoundButton button) {
        mButtons.remove(button);
    }

    public int getButtonCount() {
        return mButtons.size();
    }

    public Enumeration<CompoundButton> getElements() {
        return mButtons.elements();
    }

    public void check(int id) {
        for (CompoundButton button : mButtons) {
            if (id != -1 && button.getId() == id) {
                if (!button.isChecked()) {
                    button.setChecked(true);
                }
            } else {
                if (button.isChecked()) {
                    button.setChecked(false);
                }
            }
        }
    }

    public void check(CompoundButton button) {
        if (button == null) {
            return;
        }
        check(button.getId());
    }

    public void clearCheck() {
        check(-1);
        setCheckedId(null);
    }

    public int getCheckedId() {
        return mCheckedId;
    }

    public CompoundButton findButtonById(int id) {
        for (CompoundButton button : mButtons) {
            if (button.getId() == id) {
                return button;
            }
        }
        return null;
    }

    public CompoundButton findButtonWithTag(Object tag) {
        if (tag != null) {
            for (CompoundButton button : mButtons) {
                if (tag.equals(button.getTag())) {
                    return button;
                }
            }
        }
        return null;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    private CompoundButton.OnCheckedChangeListener mMemberCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (mCheckedId != -1) {
                    findButtonById(mCheckedId).setChecked(false);
                }
                setCheckedId(buttonView);
            } else {
                if (mCheckedId == buttonView.getId()) {
                    mCheckedId = -1;
                }
            }
        }
    };

    public static interface OnCheckedChangeListener {

        public void onCheckedChanged(CompoundButtonGroup group, CompoundButton checkedButton);

    }

}
