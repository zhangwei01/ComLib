
package com.autonavi.xm.statistics;

import android.content.Context;

import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

public class UMengAgent implements StatisticsAgent {

    private Context mContext;

    public UMengAgent(Context context) {
        mContext = context;
    }

    @Override
    public void start() {
        MobclickAgent.onResume(mContext);
    }

    @Override
    public void stop() {
        MobclickAgent.onPause(mContext);
    }

    @Override
    public void markPageStart(String page) {
        MobclickAgent.onPageStart(page);
    }

    @Override
    public void markPageEnd(String page) {
        MobclickAgent.onPageEnd(page);
    }

    @Override
    public void markEvent(String event) {
        MobclickAgent.onEvent(mContext, event);
    }

    @Override
    public void markEvent(String event, String type) {
        MobclickAgent.onEvent(mContext, event, type);
    }

    @Override
    public void markEventBegin(String event) {
        MobclickAgent.onEventBegin(mContext, event);
    }

    @Override
    public void markEventEnd(String event) {
        MobclickAgent.onEventEnd(mContext, event);
    }

    @Override
    public void markAttribute(String name, long value) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("__ct__", String.valueOf(value));
        map.put("attr", name);
        MobclickAgent.onEvent(mContext, "set_attr", map);
    }

}
