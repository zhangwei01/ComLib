
package com.autonavi.xm.app;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.autonavi.xm.app.lib.R;

public class AlertDialogFragment extends BaseDialogFragment {

    private static final String ARGUMENT_TITLE = "title";

    private static final String ARGUMENT_MESSAGE = "message";

    private static final String ARGUMENT_POSITIVE_BUTTON = "positive_button";

    private static final String ARGUMENT_NEUTRAL_BUTTON = "neutral_button";

    private static final String ARGUMENT_NEGATIVE_BUTTON = "negative_button";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.Theme_Dialog_NoTitleBar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.alert_dialog, container);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments.containsKey(ARGUMENT_TITLE)) {
            view.findViewById(R.id.top_panel).setVisibility(View.VISIBLE);
            TextView txtTitle = (TextView) view.findViewById(R.id.alert_title);
            Object value = arguments.get(ARGUMENT_TITLE);
            if (value instanceof Integer) {
                txtTitle.setText((Integer) value);
            } else if (value instanceof CharSequence) {
                txtTitle.setText((CharSequence) value);
            }
        }
        if (arguments.containsKey(ARGUMENT_MESSAGE)) {
            view.findViewById(R.id.content_panel).setVisibility(View.VISIBLE);
            TextView txtMessage = (TextView) view.findViewById(R.id.message);
            Object value = arguments.get(ARGUMENT_MESSAGE);
            if (value instanceof Integer) {
                txtMessage.setText((Integer) value);
            } else if (value instanceof CharSequence) {
                txtMessage.setText((CharSequence) value);
            }
        }
        if (arguments.containsKey(ARGUMENT_POSITIVE_BUTTON)
                || arguments.containsKey(ARGUMENT_NEUTRAL_BUTTON)
                || arguments.containsKey(ARGUMENT_NEGATIVE_BUTTON)) {
            view.findViewById(R.id.button_panel).setVisibility(View.VISIBLE);

            boolean hasPositive = false, hasNeutral = false, hasNegative = false;
            Button btnPositive = null, btnNegative = null, btnNeutral = null;
            if (arguments.containsKey(ARGUMENT_POSITIVE_BUTTON)) {
                btnPositive = (Button) view.findViewById(R.id.button_positive);
                btnPositive.setVisibility(View.VISIBLE);
                btnPositive.setOnClickListener(mOnClickListener);
                Object value = arguments.get(ARGUMENT_POSITIVE_BUTTON);
                if (value instanceof Integer) {
                    btnPositive.setText((Integer) value);
                } else if (value instanceof CharSequence) {
                    btnPositive.setText((CharSequence) value);
                }
                hasPositive = true;
            }
            if (arguments.containsKey(ARGUMENT_NEUTRAL_BUTTON)) {
                btnNeutral = (Button) view.findViewById(R.id.button_neutral);
                btnNeutral.setVisibility(View.VISIBLE);
                btnNeutral.setOnClickListener(mOnClickListener);
                Object value = arguments.get(ARGUMENT_NEUTRAL_BUTTON);
                if (value instanceof Integer) {
                    btnNeutral.setText((Integer) value);
                } else if (value instanceof CharSequence) {
                    btnNeutral.setText((CharSequence) value);
                }
                hasNeutral = true;
            }
            if (arguments.containsKey(ARGUMENT_NEGATIVE_BUTTON)) {
                btnNegative = (Button) view.findViewById(R.id.button_negative);
                btnNegative.setVisibility(View.VISIBLE);
                btnNegative.setOnClickListener(mOnClickListener);
                Object value = arguments.get(ARGUMENT_NEGATIVE_BUTTON);
                if (value instanceof Integer) {
                    btnNegative.setText((Integer) value);
                } else if (value instanceof CharSequence) {
                    btnNegative.setText((CharSequence) value);
                }
                hasNegative = true;
            }

            if (hasNeutral && hasPositive) {
                view.findViewById(R.id.button_divider_1).setVisibility(View.VISIBLE);
            }
            if (hasNegative && (hasNeutral || hasPositive)) {
                view.findViewById(R.id.button_divider_2).setVisibility(View.VISIBLE);
            }
            if (isCancelable()) {
                getDialog().setCanceledOnTouchOutside(
                        getResources().getBoolean(R.bool.config_closeDialogWhenTouchOutside));
            }

            if (hasPositive && !hasNegative && !hasNeutral) {
                btnPositive.requestFocus();
                btnPositive.setNextFocusForwardId(R.id.button_positive);
            } else if (hasPositive && hasNegative && !hasNeutral) {
                btnPositive.requestFocus();
                btnPositive.setNextFocusForwardId(R.id.button_negative);
                btnNegative.setNextFocusForwardId(R.id.button_positive);
            } else if (hasPositive && !hasNegative && hasNeutral) {
                btnPositive.requestFocus();
                btnPositive.setNextFocusForwardId(R.id.button_neutral);
                btnNeutral.setNextFocusForwardId(R.id.button_positive);
            } else if (hasPositive && hasNegative && hasNeutral) {
                btnPositive.requestFocus();
                btnPositive.setNextFocusForwardId(R.id.button_neutral);
                btnNeutral.setNextFocusForwardId(R.id.button_negative);
                btnNegative.setNextFocusForwardId(R.id.button_positive);
            } else if (!hasPositive && hasNegative && !hasNeutral) {
                btnNegative.requestFocus();
                btnNegative.setNextFocusForwardId(R.id.button_negative);
            } else if (!hasPositive && hasNegative && hasNeutral) {
                btnNeutral.requestFocus();
                btnNeutral.setNextFocusForwardId(R.id.button_negative);
                btnNegative.setNextFocusForwardId(R.id.button_neutral);
            } else if (!hasPositive && !hasNegative && hasNeutral) {
                btnNeutral.requestFocus();
                btnNeutral.setNextFocusForwardId(R.id.button_neutral);
            }
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.button_positive) {
                onDialogClick(DialogInterface.BUTTON_POSITIVE);
                dismiss();
            } else if (id == R.id.button_neutral) {
                onDialogClick(DialogInterface.BUTTON_NEUTRAL);
                dismiss();
            } else if (id == R.id.button_negative) {
                onDialogClick(DialogInterface.BUTTON_NEGATIVE);
                dismiss();
            }
        }
    };

    /**
     * This method was disabled.
     */
    @Deprecated
    @Override
    public final void setArguments(Bundle args) {
        // use setDialogArguments instead
    }

    /* package */void setDialogArguments(Bundle arguments) {
        super.setArguments(arguments);
    }

    public static class Builder {

        /* package */Bundle mArguments = new Bundle();

        private Boolean mCancelable;

        /* package */void apply(AlertDialogFragment fragment) {
            fragment.setDialogArguments(mArguments);
            if (mCancelable != null) {
                fragment.setCancelable(mCancelable);
            }
        }

        public AlertDialogFragment create() {
            AlertDialogFragment fragment = new AlertDialogFragment();
            apply(fragment);
            return fragment;
        }

        public Builder setTitle(CharSequence title) {
            mArguments.putCharSequence(ARGUMENT_TITLE, title);
            return this;
        }

        public Builder setTitle(int titleResId) {
            mArguments.putInt(ARGUMENT_TITLE, titleResId);
            return this;
        }

        public Builder setMessage(CharSequence msg) {
            mArguments.putCharSequence(ARGUMENT_MESSAGE, msg);
            return this;
        }

        public Builder setMessage(int msgResId) {
            mArguments.putInt(ARGUMENT_MESSAGE, msgResId);
            return this;
        }

        public Builder setPositiveButton(CharSequence text) {
            mArguments.putCharSequence(ARGUMENT_POSITIVE_BUTTON, text);
            return this;
        }

        public Builder setPositiveButton(int textResId) {
            mArguments.putInt(ARGUMENT_POSITIVE_BUTTON, textResId);
            return this;
        }

        public Builder setNeutralButton(CharSequence text) {
            mArguments.putCharSequence(ARGUMENT_NEUTRAL_BUTTON, text);
            return this;
        }

        public Builder setNeutralButton(int textResId) {
            mArguments.putInt(ARGUMENT_NEUTRAL_BUTTON, textResId);
            return this;
        }

        public Builder setNegativeButton(CharSequence text) {
            mArguments.putCharSequence(ARGUMENT_NEGATIVE_BUTTON, text);
            return this;
        }

        public Builder setNegativeButton(int textResId) {
            mArguments.putInt(ARGUMENT_NEGATIVE_BUTTON, textResId);
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            mCancelable = cancelable;
            return this;
        }

    }

}
