
package com.autonavi.xm.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;

import com.autonavi.xm.app.lib.R;

public class CompoundExpandableListView extends RelativeLayout {

    private CircularExpandableListView mExpandableListViewListView;

    private CompoundScrollbar mScrollbar;

    public CompoundExpandableListView(Context context) {
        this(context, null);
    }

    public CompoundExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.compound_expand_list_view, this);

        mExpandableListViewListView = (CircularExpandableListView) findViewById(R.id.expand_list);
        mScrollbar = (CompoundScrollbar) findViewById(R.id.scrollbar);
        mScrollbar.setAbsListView(mExpandableListViewListView);
    }

    public CircularExpandableListView getListView() {
        return mExpandableListViewListView;
    }

    public void setAdapter(ListAdapter adapter) {
        mExpandableListViewListView.setAdapter(adapter);
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
