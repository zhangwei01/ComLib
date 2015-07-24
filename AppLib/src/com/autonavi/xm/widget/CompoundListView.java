
package com.autonavi.xm.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;

import com.autonavi.xm.app.lib.R;

public class CompoundListView extends RelativeLayout {

    private final CircularListView mListView;

    private final CompoundScrollbar mScrollbar;

    public CompoundListView(Context context) {
        this(context, null);
    }

    public CompoundListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.compound_list_view, this);

        mListView = (CircularListView) findViewById(R.id.list);
        mScrollbar = (CompoundScrollbar) findViewById(R.id.scrollbar);
        mScrollbar.setAbsListView(mListView);
    }

    public CircularListView getListView() {
        return mListView;
    }

    public void setAdapter(ListAdapter adapter) {
        mListView.setAdapter(adapter);
    }

    public void setPageNumberEnabled(boolean enabled) {
        mScrollbar.setPageNumberEnabled(enabled);
    }

    public void setTitleView(int resId) {
        ViewStub stubTitle = (ViewStub) findViewById(R.id.stub_list_title);
        stubTitle.setLayoutResource(resId);
        stubTitle.inflate();
        findViewById(R.id.list_title_divider).setVisibility(VISIBLE);
    }

}
