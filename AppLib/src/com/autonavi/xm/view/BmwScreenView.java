
package com.autonavi.xm.view;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.autonavi.xm.app.lib.R;
import com.autonavi.xm.content.SharedIntent;
import com.autonavi.xm.widget.LinkedListView;
import com.autonavi.xm.widget.SelectionLinker;

/**
 * 精能电子宝马版--主菜单通用界面模板，参见:NavigationLib库工程的NaviScreen类
 * 
 * @author kaiyuan.zhang
 * @since 2013-6-20
 */
public class BmwScreenView {

    /**
     * 外部要给ListView的Adapter设置数据时，在接口的实现方法里面调用ListAdapter的setData方法，
     * 能确保SelectionLinker绘制完成后设置数据
     * 
     * @author kaiyuan.zhang
     * @since 2013-6-21
     */
    public interface OnAdapterSetDataListener {
        void onAdapterSetData();
    }

    protected Activity mActivity;

    private LayoutInflater mLayoutInflater;

    private View mScreenView;

    private View mGoBackView;

    private View mBackToMapView;

    private View mLeftSeparator;

    private View mRightSeparator;

    //标题栏左边内容的父容器，用于控制与SelectionLinker的Margin距离
    private View mTitleLeftParent;

    private TextView mTitle;

    private FrameLayout mTitleRightContent;

    private LinkedListView mListView;

    private SelectionLinker mSelectionLinker;

    public BmwScreenView(Context context) {
        if (!(context instanceof Activity)) {
            throw new IllegalArgumentException("The context must be an \"Activity\"!");
        }
        mActivity = (Activity) context;
        mLayoutInflater = LayoutInflater.from(context);

        mScreenView = mLayoutInflater.inflate(R.layout.bmw_screen_view, null);
        mTitleLeftParent = mScreenView.findViewById(R.id.title_left_parent);
        mGoBackView = mScreenView.findViewById(R.id.goBack);
        mBackToMapView = mScreenView.findViewById(R.id.back_to_map);
        mLeftSeparator = mScreenView.findViewById(R.id.left_separator);
        mRightSeparator = mScreenView.findViewById(R.id.right_separator);
        mTitle = (TextView) mScreenView.findViewById(R.id.title);
        mTitleRightContent = (FrameLayout) mScreenView.findViewById(R.id.title_right_content);
        mSelectionLinker = (SelectionLinker) mScreenView.findViewById(R.id.selection_linker);
        mListView = (LinkedListView) mScreenView.findViewById(R.id.list);
        mGoBackView.setOnClickListener(mOnClickListener);
        mBackToMapView.setOnClickListener(mOnClickListener);

        link();
    }

    //连接ListView和SelectionLinker
    private void link() {
        mSelectionLinker.link(mListView);
        mSelectionLinker.post(new Runnable() {

            @Override
            public void run() {
                //已在bmw_screen_view.xml的title_left_parent中设置了65dp的间距，不动态设置间距
                //                MarginLayoutParams params = (MarginLayoutParams) mTitleLeftParent.getLayoutParams();
                //                params.setMargins((int) (mSelectionLinker.getIntrinsicWidth() * 2f / 5f), 0, 0, 0);
                //                mTitleLeftParent.setLayoutParams(params);
                if (mOnAdapterSetDataListener != null) {
                    mOnAdapterSetDataListener.onAdapterSetData();
                }
            }
        });
    }

    private OnAdapterSetDataListener mOnAdapterSetDataListener;

    public void setOnAdapterSetDataListener(OnAdapterSetDataListener listener) {
        mOnAdapterSetDataListener = listener;
    }

    /**
     * 获取界面布局
     * 
     * @return View 视图
     */
    public View getScreenView() {
        return mScreenView;
    }

    public SelectionLinker getSelectionLinker() {
        return mSelectionLinker;
    }

    public LinkedListView getListView() {
        return mListView;
    }

    /**
     * 设置返回按钮的显隐，默认状态为VISIBLE
     * 
     * @param visibility true显示，否则不显示
     */
    public void setBackVisibility(boolean visibility) {
        mGoBackView.setVisibility(visibility ? View.VISIBLE : View.GONE);
        if (visibility && mTitle.getText() != null) {
            mLeftSeparator.setVisibility(View.VISIBLE);
        } else {
            mLeftSeparator.setVisibility(View.GONE);
        }
    }

    /**
     * 设置返回地图按钮的显隐，默认状态为VISIBLE
     * 
     * @param visibility true显示，否则不显示
     */
    public void setBackToMapVisibility(boolean visibility) {
        mBackToMapView.setVisibility(visibility ? View.VISIBLE : View.GONE);
        if (visibility && mTitleRightContent.getChildCount() > 0) {
            mRightSeparator.setVisibility(View.VISIBLE);
        } else {
            mRightSeparator.setVisibility(View.GONE);
        }
    }

    public void setRightSeparatorVisibility(boolean visibility) {
        mRightSeparator.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    public void setTitle(int textResId) {
        setTitle(mActivity.getResources().getString(textResId));
    }

    public void setTitle(String title) {
        mTitle.setText(title);
        if (title != null && mGoBackView.getVisibility() == View.VISIBLE) {
            mLeftSeparator.setVisibility(View.VISIBLE);
        }
    }

    public void setTitleIcon(int iconResId) {
        mTitle.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);
    }

    /**
     * 设置标题栏右边的内容，在返回地图按钮的左边
     * 
     * @param layoutResId 布局资源ID
     */
    public void setTitleRightContent(int layoutResId) {
        setTitleRightContent(layoutResId > 0 ? mLayoutInflater.inflate(layoutResId,
                mTitleRightContent, false) : null);
    }

    /**
     * 设置标题栏右边的内容，在返回地图按钮的左边
     * 
     * @param view 视图
     */
    public void setTitleRightContent(View view) {
        setTitleRightContent(view, view != null ? view.getLayoutParams() : null);
    }

    /**
     * 设置标题栏右边的内容，在返回地图按钮的左边
     * 
     * @param view 控件
     * @param params 控件的Layout参数
     */
    public void setTitleRightContent(View view, ViewGroup.LayoutParams params) {
        mTitleRightContent.removeAllViews();
        if (view != null) {
            mTitleRightContent.setVisibility(View.VISIBLE);
            mTitleRightContent.addView(view, getDefaultLayoutParams(params));
            mRightSeparator
                    .setVisibility(mBackToMapView.getVisibility() == View.VISIBLE ? View.VISIBLE
                            : View.GONE);
        }
    }

    private ViewGroup.LayoutParams getDefaultLayoutParams(ViewGroup.LayoutParams params) {
        return params != null ? params : new FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void backToMap() {
        try {
            mActivity.startActivity(mActivity.getPackageManager().getLaunchIntentForPackage(
                    SharedIntent.PACKAGE_NAVIGATION));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mActivity, R.string.application_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v == mGoBackView) {
                if (mActivity != null) {
                    mActivity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_BACK));
                    mActivity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                            KeyEvent.KEYCODE_BACK));
                }
            } else if (v == mBackToMapView) {
                backToMap();
            }
        }
    };

    /**
     * 主菜单和系统设置界面通用的Adapter，继承时需要实现getItemView()方法，返回的是每一项的布局
     * 
     * @author kaiyuan.zhang
     * @since 2013-6-21
     */
    public static abstract class LinkedListAdapter extends BaseAdapter {

        protected Context mContext;

        protected LayoutInflater mInflater;

        private int[] mItemIds;

        private int[] mImageRes;

        /**
         * 设置Adapter的通用属性
         * 
         * @param itemIds 每一项的ID，在onItemClick中可以判断是哪项被点击
         * @param imageRes 
         *            用于在SelectionLinker中切换图片时所用到的资源，可以为null，如果为null的话就需要单独设置linker的图片资源
         */
        public void setBaseData(int[] itemIds, int[] imageRes) {
            mItemIds = itemIds;
            mImageRes = imageRes;
            notifyDataSetChanged();
        }

        public LinkedListAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mItemIds != null ? mItemIds.length : 0;
        }

        @Override
        public Object getItem(int position) {
            //TODO 重新定义？
            return mItemIds != null ? mItemIds[position] : null;
        }

        @Override
        public long getItemId(int position) {
            return mItemIds != null ? mItemIds[position] : -1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = getItemView(position, convertView, parent);
            v.setTag(mImageRes != null ? mImageRes[position] : null);
            return v;
        }

        /**
         * 在这里设置自定义的布局
         * 
         * @param position
         * @param convertView
         * @param parent
         * @return
         */
        protected abstract View getItemView(int position, View convertView, ViewGroup parent);
    }

}
