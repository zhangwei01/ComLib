
package com.autonavi.xm.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.autonavi.xm.app.lib.R;

public class CompoundGridView extends LinearLayout {

    private GridView mGridView;

    private CompoundScrollbar mScrollbar;

    public CompoundGridView(Context context) {
        this(context, null);
    }

    public CompoundGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.compound_grid_view, this);

        mGridView = (GridView) findViewById(R.id.grid);
        mScrollbar = (CompoundScrollbar) findViewById(R.id.scrollbar);
        mScrollbar.setAbsListView(mGridView);
    }

    public GridView getGridView() {
        return mGridView;
    }

    public void setPageNumberEnabled(boolean enabled) {
        mScrollbar.setPageNumberEnabled(enabled);
    }
}
