
package com.autonavi.xm.download;

/**
 * 下载回调接口，定义了回调结果常量和回调监听接口
 * 
 * @author if
 */
public interface DownloadInterface {

    /**
     * 加载结果：未定义
     */
    public static final int PREPARE_RESULT_UNDEFINED = -1;

    /**
     * 加载结果：成功
     */
    public static final int PREPARE_RESULT_CREATE_SUCCESS = 0;

    /**
     * 加载结果：异常
     */
    public static final int PREPARE_RESULT_CREATE_EXCEPTION = 1;

    /**
     * 加载结果：超时
     */
    public static final int PREPARE_RESULT_TIMEOUT = 2;

    /**
     * 加载结果：响应错误
     */
    public static final int PREPARE_RESULT_RESPONSE_ERROR = 3;

    /**
     * 加载结果：续传成功
     */
    public static final int PREPARE_RESULT_RESUME_SUCCESS = 4;

    /**
     * 加载结果：续传失败
     */
    public static final int PREPARE_RESULT_RESUME_FAIL = 5;

    /**
     * 下载回调监听：下载任务已启动
     * 
     * @author if
     */
    public static interface OnStartedListener {

        void onStarted(int id);

    }

    /**
     * 下载回调监听：下载任务已停止
     * 
     * @author if
     */
    public static interface OnStoppedListener {

        void onStopped(int id);

    }

    /**
     * 下载回调监听：下载任务已准备完成
     * 
     * @author if
     */
    public static interface OnPreparedListener {

        void onPrepared(int id, int resultCode);

    }

    /**
     * 下载回调监听：下载进度已更新
     * 
     * @author if
     */
    public static interface OnProgressUpdateListener {

        void onProgressUpdate(int id, long downSize);
    }

    /**
     * 下载回调监听：下载任务已完成
     * 
     * @author if
     */
    public static interface OnCompletionListener {

        void onCompletion(int id);

    }

    /**
     * 下载回调监听：下载发生错误
     * 
     * @author if
     */
    public static interface OnErrorListener {

        void onError(int id, int errorCode);

    }

    /**
     * 下载时网络断开的监听
     * 
     * @author hanwei.chen
     * @since 2013-9-23 下午3:08:21
     */
    public static interface OnWifiConnectListener {
        void onWifiConnect();
    }
}
