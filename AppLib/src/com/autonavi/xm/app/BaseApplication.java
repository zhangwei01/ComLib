
package com.autonavi.xm.app;

import android.app.Application;
import android.os.Environment;
import android.os.IAt3232Service;

import com.autonavi.xm.error.report.ErrorHandler;
import com.autonavi.xm.statistics.StatisticsAgent;
import com.autonavi.xm.statistics.UMengAgent;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Stack;

public class BaseApplication extends Application implements UncaughtExceptionHandler {

    private static final String DEFAULT_TRACES_DIR = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/traces";

    private Stack<BaseActivity> mActivityStack = new Stack<BaseActivity>();

    private ErrorHandler mErrorHandler;

    private StatisticsAgent mStatisticsAgent;

    @Override
    public void onCreate() {
        //checkDevAuth();

        mErrorHandler = new ErrorHandler(this);
        mErrorHandler.setTracesDir(DEFAULT_TRACES_DIR);
        mErrorHandler.setUncaughtExceptionHandler(this);

        mStatisticsAgent = new UMengAgent(getApplicationContext());
    }

    protected StatisticsAgent getStatisticsAgent() {
        return mStatisticsAgent;
    }

    public void pushActivity(BaseActivity activity) {
        mActivityStack.push(activity);
    }

    public void removeActivity(BaseActivity activity) {
        mActivityStack.remove(activity);
    }

    public BaseActivity[] getStackedActivities() {
        BaseActivity[] acitivities = new BaseActivity[mActivityStack.size()];
        mActivityStack.toArray(acitivities);
        return acitivities;
    }

    /**
     * 设置跟踪文件、Log等的存放目录
     * 
     * @param dir 存放目录
     */
    public void setTracesDir(String dir) {
        mErrorHandler.setTracesDir(dir);
    }

    private void finishAllActivities() {
        BaseActivity[] activities = getStackedActivities();
        if (activities != null) {
            for (BaseActivity activity : activities) {
                activity.finish();
            }
        }
    }

    //IC加密检查
    private void checkDevAuth() {
        new Thread() {

            @Override
            public void run() {
                int value = 0;
                try {
                    value = ((IAt3232Service) getSystemService("at3232")).authDev();
                } catch (Exception e) {
                    //auth failed
                }
                if (value != 1) {
                    finishAllActivities();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }

        }.start();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        //出现异常时，finish所有的Activity，防止反复重启
        finishAllActivities();
    }

}
