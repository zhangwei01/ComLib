
package com.autonavi.xm.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.autonavi.xm.app.lib.R;

public class ProgressDialogFragment extends AlertDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup contentGroup = (ViewGroup) view.findViewById(R.id.content_panel);
        contentGroup.removeAllViews();
        inflater.inflate(R.layout.alert_dialog_progress, contentGroup);
        return view;
    }

    public static final class Builder extends AlertDialogFragment.Builder {

        @Override
        public ProgressDialogFragment create() {
            ProgressDialogFragment fragment = new ProgressDialogFragment();
            apply(fragment);
            return fragment;
        }

    }

}
