
package com.autonavi.xm.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件下载类，用来执行下载任务，支持续传、多线程下载
 * 
 * @author if
 */
public class FileDownloader {

    /**
     * 超时时间，毫秒
     */
    private static final int TIMEOUT_MILLIS = 20 * 1000;

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_TIMES = 20;

    /**
     * 最小下载字节数，如果小于此数则视为无效连接，尝试重建连接进行下载
     */
    private static final int MIN_DOWNLOAD_SIZE = 10;

    /**
     * 重试等待时间，毫秒
     */
    private static final int RETRY_DELAY_MILLIS = 3 * 1000;

    /**
     * 最大重试等待时间，毫秒
     */
    private static final int MAX_RETRY_DELAY_MILLIS = 60 * 1000;

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 25;

    /**
     * 下载未完成文件的后缀
     */
    private static final String DOWNLOAD_FILE_SUFFIX = ".gddown";

    private static final String EXCEPTION_MESSAGE_START_GREATER_THAN_END = "Start > End";

    private static final String EXCEPTION_MESSAGE_SAVE_FILE_AND_SAVE_DIR_IS_NULL = "Save file and save directory can not both be null";

    /**
     * ID，方便下载任务的管理
     */
    private int id;

    /**
     * 文件保存目录
     */
    private final File mSaveDir;

    /**
     * 本地保存文件
     */
    private File mSaveFile;

    /**
     * 本地保存的临时文件，即加上后缀的文件
     */
    private File mTempSaveFile;

    /**
     * 下载文件名
     */
    private String mFileName;

    /**
     * 下载源URL
     */
    private URL mSourceUrl;

    /**
     * 加载是否成功，如果失败，则无法启动下载
     */
    private boolean mIsLoadSuccess = false;

    /**
     * 文件总大小
     */
    private long mTotalSize;

    /**
     * 已下载大小
     */
    private long mDownloadedSize;

    /**
     * 下载线程个数，目前并未向外部提供设定接口
     */
    private int mThreadNum = 1;

    /**
     * 记录续传所需的信息
     */
    private FileRecorder mFileRecorder;

    private boolean mIsPrepareStarted = false;

    private boolean mIsPrepared = false;

    private boolean mIsStarted = false;

    private boolean mIsDownloading = false;

    /**
     * 外部回调接口对象
     */
    private Callback mCallback;

    /**
     * 下载执行体数组，用来控制正在下载的线程
     */
    private DownloadRunner[] mDownloadRunners;

    /**
     * 只使用文件保存路径和下载回调来初始化，用续传信息还原下载任务时使用
     * 
     * @param saveFilePath 下载文件保存路径
     * @param callback 下载回调
     */
    public FileDownloader(String saveFilePath, Callback callback) {
        this(new File(saveFilePath), null, null, callback);
    }

    /**
     * 用下载保存File和下载回调来初始化，用续传信息还原下载任务时使用
     * 
     * @param saveFile 下载保存File
     * @param callback 下载回调
     */
    public FileDownloader(File saveFile, Callback callback) {
        this(saveFile, null, null, callback);
    }

    /**
     * 指定下载URL，保存路径和回调来初始化，文件名通过解析HTTP链接来设定
     * 
     * @param sourceUrl 下载保存路径
     * @param saveDirPath 下载保存路径
     * @param callback 下载回调
     */
    public FileDownloader(URL sourceUrl, String saveDirPath, Callback callback) {
        this(null, sourceUrl, saveDirPath, callback);
    }

    /**
     * 内部构造方法，saveFile和saveDirPath必须指定一项
     * 
     * @param saveFile 下载保存File
     * @param sourceUrl 下载保存路径
     * @param saveDirPath 下载保存路径
     * @param callback 下载回调
     */
    private FileDownloader(File saveFile, URL sourceUrl, String saveDirPath, Callback callback) {
        mSaveFile = saveFile;
        mSaveDir = saveDirPath == null ? null : new File(saveDirPath);
        if (saveFile == null && saveDirPath == null) {
            throw new IllegalArgumentException(EXCEPTION_MESSAGE_SAVE_FILE_AND_SAVE_DIR_IS_NULL);
        }
        mSourceUrl = sourceUrl;
        mCallback = callback;
        if (mSaveFile != null) {
            prepare();
        }
    }

    /**
     * 设置ID
     * 
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * 获取ID
     * 
     * @return ID
     */
    public int getId() {
        return id;
    }

    /**
     * 获取要下载文件的实际大小
     * 
     * @return 文件总大小
     */
    public long getTotalSize() {
        return mTotalSize;
    }

    /**
     * 获取已下载大小
     * 
     * @return 已下载大小
     */
    public synchronized long getDownloadedSize() {
        return mDownloadedSize;
    }

    /**
     * 获取文件名，必须在onPrepared回调之后才能获取
     * 
     * @return 文件名
     */
    public String getFileName() {
        return mFileName;
    }

    /**
     * 获取下载的本地文件File
     * 
     * @return 本地文件File
     */
    public File getSaveFile() {
        return mSaveFile;
    }

    /**
     * 获取下载源URL
     * 
     * @return 源URL
     */
    public URL getSourceUrl() {
        return mSourceUrl;
    }

    /**
     * 删除无用的文件（临时文件、续传记录文件），一般是在删除该下载任务或下载完成时调用
     */
    public void removeUselessFiles() {
        if (mTempSaveFile != null && mTempSaveFile.exists()) {
            mTempSaveFile.delete();
        }
        if (mFileRecorder != null) {
            mFileRecorder.removeRecordFile();
        }
    }

    /**
     * 设置回调接口
     * 
     * @param callback 回调接口
     */
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * 增加总的已下载大小并通知回调
     * 
     * @param size 一次读取保存的大小
     */
    synchronized void increaseDownloadedSize(long size) {
        mDownloadedSize += size;
        onProgressUpdate(mDownloadedSize);
        if (mDownloadedSize >= mTotalSize) {
            onCompletion();
        }
    }

    /**
     * 将各个下载线程已下载的大小保存下来，供续传使用
     * 
     * @param threadId 下载线程ID
     * @param downSize 该线程已下载的大小
     */
    void recordDownloadSize(int threadId, long downSize) {
        mFileRecorder.saveDownloadSize(threadId, downSize);
    }

    /**
     * 读取指定下载线程已下载的大小，续传恢复下载时调用
     * 
     * @param threadId 下载线程ID
     * @return 从续传记录文件读取到的已下载大小
     */
    protected long readDownloadSize(int threadId) {
        return mFileRecorder.readDownloadSize(threadId);
    }

    protected void onStarted() {
        if (mCallback != null) {
            mCallback.onStarted(this);
        }
    }

    protected void onStopped() {
        if (mCallback != null) {
            mCallback.onStopped(this);
        }
    }

    protected void onPrepared(int resultCode) {
        if (mCallback != null) {
            mCallback.onPrepared(this, resultCode);
        }
    }

    protected void onCompletion() {
        mTempSaveFile.renameTo(mSaveFile);
        if (mCallback != null) {
            mCallback.onCompletion(this);
        }
        mIsPrepareStarted = false;
        mIsStarted = false;
        mIsDownloading = false;
    }

    protected void onProgressUpdate(long downSize) {
        if (mCallback != null) {
            mCallback.onProgressUpdate(this, downSize);
        }
    }

    protected void onError(int exceptionCode) {
        if (mCallback != null) {
            mCallback.onError(this, exceptionCode);
        }
    }

    private PrepareRunner mPrepareRunner;

    /**
     * 前期准备，构造实例时调用。执行的操作参看PrepareRunner说明
     */
    private void prepare() {
        if (mIsPrepared || mIsPrepareStarted) {
            return;
        }
        mIsPrepareStarted = true;
        mPrepareRunner = new PrepareRunner();
        new Thread(mPrepareRunner).start();
    }

    private StartRunner mStartRunner;

    /**
     * 开始下载，如果尚未Prepared，则会在Prepared之后自动开始
     */
    public void start() {
        if (!mIsPrepared) {
            prepare();
        }
        if (!mIsStarted) {
            onStarted();
        }
        mIsStarted = true;
        if (!mIsLoadSuccess || mTotalSize <= 0 || mIsDownloading) {
            return;
        }
        mIsDownloading = true;
        mStartRunner = new StartRunner();
        new Thread(mStartRunner).start();
    }

    /**
     * 停止下载
     */
    public void stop() {
        if (mPrepareRunner != null) {
            mPrepareRunner.quit();
        }
        if (mStartRunner != null) {
            mStartRunner.quit();
        }
        DownloadRunner[] downloadRunners = mDownloadRunners;
        if (downloadRunners != null) {
            for (DownloadRunner runner : downloadRunners) {
                if (runner != null) {
                    runner.quit();
                }
            }
        }
        mIsPrepareStarted = false;
        mIsStarted = false;
        mIsDownloading = false;
        onStopped();
    }

    /**
     * 下载任务是否已开始
     * 
     * @return 已开始返回true，否则返回false
     */
    public boolean isStarted() {
        return mIsStarted;
    }

    /**
     * 获取文件名，优先通过响应头解析文件名，获取不到则通过URL地址解析文件名，再获取不到则利用UUID生成一个文件名
     * 
     * @param conn 已连接并响应OK的HttpURLConnection
     */
    private String getFileName(HttpURLConnection conn) {
        String filename = null;
        //通过响应头解析文件名
        for (int i = 0;; i++) {
            String mine = conn.getHeaderFieldKey(i);
            if (i > 0 && mine == null) {
                break;
            }
            //响应头中content-disposition如果包含filename的描述则取为文件名
            if ("content-disposition".equalsIgnoreCase(mine)) {
                Matcher m = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase());
                if (m.find()) {
                    return URLDecoder.decode(m.group(1));
                }
            }
        }
        //通过响应头获取不到文件名，则通过URL解析文件名。以URL最后一个‘/’之后的字符串作为文件名
        if (filename == null || filename.length() <= 0) {
            String urlStr = URLDecoder.decode(conn.getURL().toString());
            filename = urlStr.substring(urlStr.lastIndexOf('/') + 1);
        }
        //以上手段都得不到文件名，则使用UUID生成一个文件名
        if (filename == null || filename.length() <= 0) {
            filename = UUID.randomUUID() + ".tmp";
        }
        return filename;
    }

    /**
     * 启动一个下载线程
     * 
     * @param threadId 线程ID
     * @return 已下载完成则返回false，否则成功启动后返回true
     */
    private boolean launchDownloadThread(int threadId) {
        long totalSize = mTotalSize;
        int threadNum = mThreadNum;
        long partSize = totalSize / threadNum;
        long remainSize = totalSize - partSize * threadId;
        long downloadedSize = readDownloadSize(threadId);
        if (downloadedSize >= partSize || downloadedSize >= remainSize) {
            return false;
        }
        long startPos = partSize * threadId;//分配的起始位置
        long endPos = startPos + partSize - 1;
        DownloadRunner runner = new DownloadRunner(mSourceUrl, mTempSaveFile, startPos, endPos,
                downloadedSize, threadId);
        mDownloadRunners[threadId] = runner;
        new Thread(runner).start();
        return true;
    }

    /**
     * 启动下载的Runnable，指定文件大小分配物理空间（耗时），并启动下载线程，在子线程中执行防止主线程阻塞。
     * （目前取消分配物理空间的操作，耗时且无法中断，此时执行删除文件会卡住甚至出错）
     * 
     * @author if
     */
    private class StartRunner implements Runnable {

        private boolean mIsRunning = true;

        public void quit() {
            mIsRunning = false;
        }

        @Override
        public void run() {
            long totalSize = mTotalSize;
            if (totalSize <= 0) {
                return;
            }
            /*TODO how to set file length
             * try {
                RandomAccessFile rndSaveFile = new RandomAccessFile(mTempSaveFile, "rw");
                if (totalSize > 0) {
                    rndSaveFile.setLength(totalSize);
                    rndSaveFile.close();
                }
            } catch (IOException e) {
            }*/
            if (!mIsRunning) {
                return;
            }
            int threadNum = mThreadNum;
            mDownloadRunners = new DownloadRunner[threadNum];
            for (int i = 0; mIsRunning && i < threadNum; i++) {
                launchDownloadThread(i);
            }
        }

    }

    /**
     * 下载前准备的Runnable。如果有指定保存文件，则通过续传记录，恢复下载；否则用指定的URL和存放目录来初始化下载任务（有HTTP连接，耗时）
     * 
     * @author if
     */
    private class PrepareRunner implements Runnable {

        private boolean mIsRunning = true;

        public void quit() {
            mIsRunning = false;
        }

        private int mRetryCount;

        /**
         * 重试等待，休眠一段时间
         */
        private void retryDelay() {
            mRetryCount = Math.max(++mRetryCount % MAX_RETRY_COUNT, 1);
            try {
                Thread.sleep(Math.min(RETRY_DELAY_MILLIS * mRetryCount, MAX_RETRY_DELAY_MILLIS));
            } catch (InterruptedException e) {
            }
        }

        @Override
        public void run() {
            int resultCode = DownloadInterface.PREPARE_RESULT_UNDEFINED;
            FileRecorder fileRecorder = null;
            if (mSaveFile != null) {
                /*
                 * 有指定保存文件，从续传记录FileRecorder恢复，需要恢复的：文件总大小,下载线程数,下载源URL 
                 */
                try {
                    mTempSaveFile = new File(mSaveFile.getAbsolutePath().concat(
                            DOWNLOAD_FILE_SUFFIX));
                    fileRecorder = mFileRecorder = new FileRecorder(mSaveFile, true);
                    mTotalSize = fileRecorder.readTotalSize();
                    int threadNum = fileRecorder.readThreadNum();
                    if (threadNum > 0) {
                        mThreadNum = threadNum;
                    }
                    String urlStr = fileRecorder.readDownloadUrl();
                    if (urlStr != null) {
                        mSourceUrl = new URL(urlStr);
                    }
                    mFileName = mSaveFile.getName();
                    if (mTotalSize > 0) {
                        resultCode = DownloadInterface.PREPARE_RESULT_RESUME_SUCCESS;
                        mIsLoadSuccess = true;
                    }
                } catch (IOException e) {
                    resultCode = DownloadInterface.PREPARE_RESULT_RESUME_FAIL;
                }
            }
            if (!mIsLoadSuccess && mSourceUrl != null && mSaveDir != null) {
                /*
                 * 用HttpURLConnection获取下载所需的信息：文件总大小,文件名；创建续传记录实例，记录相关信息。
                 */

                //循环执行直到加载成功，加载失败（一般是网络问题），则等待一段时间后重试
                while (mIsRunning) {
                    HttpURLConnection conn = null;
                    try {
                        conn = createHttpConnection(mSourceUrl);
                        conn.connect();
                        if (!mIsRunning) {
                            return;
                        }
                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK && mIsRunning) {
                            mTotalSize = conn.getContentLength();
                            mFileName = getFileName(conn);
                            if (!mSaveDir.exists()) {//确保下载目录已存在
                                mSaveDir.mkdirs();
                            }
                            mSaveFile = new File(mSaveDir, mFileName);
                            mTempSaveFile = new File(mSaveDir,
                                    mFileName.concat(DOWNLOAD_FILE_SUFFIX));
                            fileRecorder = mFileRecorder = new FileRecorder(mSaveFile, false);
                            fileRecorder.saveThreadNum(mThreadNum);
                            fileRecorder.saveTotalSize(mTotalSize);
                            fileRecorder.saveDownloadUrl(mSourceUrl.toString());
                            resultCode = DownloadInterface.PREPARE_RESULT_CREATE_SUCCESS;
                            mIsLoadSuccess = true;
                        } else {
                            resultCode = DownloadInterface.PREPARE_RESULT_RESPONSE_ERROR;
                        }
                    } catch (SocketTimeoutException e) {
                        resultCode = DownloadInterface.PREPARE_RESULT_TIMEOUT;
                    } catch (IOException e) {
                        resultCode = DownloadInterface.PREPARE_RESULT_CREATE_EXCEPTION;
                    } finally {
                        if (conn != null) {
                            conn.disconnect();
                        }
                    }
                    if (mIsLoadSuccess) {
                        break;
                    } else {
                        retryDelay();
                    }
                }
            }
            if (mIsRunning && mIsPrepareStarted) {
                onPrepared(resultCode);
                if (mIsLoadSuccess && fileRecorder != null) {
                    mIsPrepared = true;
                    increaseDownloadedSize(fileRecorder.readTotalDownloadSize());
                    if (mIsStarted) {
                        start();
                    }
                }
                mIsPrepareStarted = false;
            }
        }

    }

    /**
     * 执行下载的Runnable。创建HTTP连接，从服务器读取数据，用RandomAccessFile保存到本地，
     * 每次保存操作都需要将已下载大小记录下来。
     * 
     * @author if
     */
    private class DownloadRunner implements Runnable {

        /**
         * 读取数据的最大缓存字节数
         */
        private static final int MAX_BUFFER_BYTES = 1024 * 1024;

        /**
         * 下载源URL
         */
        private final URL mSourceUrl;

        /**
         * 本地保存目标文件
         */
        private final File mTargetFile;

        /**
         * 下载的起始位置
         */
        private final long mStartPos;

        /**
         * 下载的终点位置
         */
        private final long mEndPos;

        /**
         * 已下载大小
         */
        private long mDownloadedSize;

        /**
         * 线程ID，即下载线程索引值
         */
        private final int mThreadId;

        /**
         * 是否正在运行，用来中断执行
         */
        private boolean mIsRunning;

        /**
         * 重试次数
         */
        private int mRetryCount;

        public DownloadRunner(URL sourceUrl, File targetFile, long startPos, long endPos,
                long downloadedSize, int threadId) {
            mSourceUrl = sourceUrl;
            mTargetFile = targetFile;
            mStartPos = startPos;
            mEndPos = endPos;
            mDownloadedSize = downloadedSize;
            mThreadId = threadId;
            mIsRunning = true;
        }

        /**
         * 退出下载线程
         */
        public void quit() {
            mIsRunning = false;
        }

        /**
         * 重试等待，休眠一段时间
         */
        private void retryDelay() {
            mRetryCount = Math.max(++mRetryCount % MAX_RETRY_COUNT, 1);
            try {
                Thread.sleep(Math.min(RETRY_DELAY_MILLIS * mRetryCount, MAX_RETRY_DELAY_MILLIS));
            } catch (InterruptedException e) {
            }
        }

        /**
         * 下载主方法，创建HTTP连接，从服务器读取数据，用RandomAccessFile保存到本地，每次保存操作都需要将已下载大小记录下来。
         * 
         * @return 下载完成或退出下载返回true，否则返回false
         */
        private boolean download() {
            boolean isFinished = true;
            InputStream inStream = null;
            RandomAccessFile saveRndFile = null;
            int threadId = mThreadId;
            long downloadedSize = mDownloadedSize;
            HttpURLConnection conn = null;
            try {
                long startPos = mStartPos + downloadedSize;//初始起始位置+已下载大小=实际起始位置
                long endPos = mEndPos;
                saveRndFile = new RandomAccessFile(mTargetFile, "rw");
                saveRndFile.seek(startPos);
                conn = createHttpConnection(mSourceUrl, startPos, endPos);
                inStream = conn.getInputStream();
                byte[] buffer = new byte[Math.max(1,
                        Math.min(MAX_BUFFER_BYTES, inStream.available()))];
                int readTotal = 0;
                int invalidCount = 0;
                startPos = mStartPos;
                while (mIsRunning && (startPos + downloadedSize < endPos)
                        && (readTotal = inStream.read(buffer)) != -1) {
                    if (!mIsRunning) {
                        break;
                    }
                    saveRndFile.write(buffer, 0, readTotal);
                    downloadedSize += readTotal;
                    recordDownloadSize(threadId, downloadedSize);
                    increaseDownloadedSize(readTotal);
                    if (readTotal < MIN_DOWNLOAD_SIZE) {
                        if (++invalidCount > MAX_RETRY_TIMES) {
                            isFinished = false;
                            break;
                        }
                    } else {
                        mRetryCount = 0;
                    }
                }
            } catch (IOException e) {
                if(mCallback != null){
                    mCallback.onWifiConnect();
                }
                isFinished = false;
            } finally {
                try {
                    if (inStream != null) {
                        inStream.close();
                    }
                    if (saveRndFile != null) {
                        saveRndFile.close();
                    }
                } catch (IOException e) {
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
            mDownloadedSize = downloadedSize;
            return isFinished;
        }

        @Override
        public void run() {
            //循环调用下载方法，直到下载完成或退出下载
            while (mIsRunning) {
                boolean isFinished = download();
                if (isFinished) {
                    return;
                } else {
                    retryDelay();
                }
            }
        }
    }

    /**
     * 下载回调接口
     * 
     * @author if
     */
    public static interface Callback {

        /**
         * 下载任务已开始
         * 
         * @param fileDownloader 任务实例
         */
        public void onStarted(FileDownloader fileDownloader);

        /**
         * 下载任务已停止
         * 
         * @param fileDownloader 任务实例
         */
        public void onStopped(FileDownloader fileDownloader);

        /**
         * 下载任务加载完成
         * 
         * @param fileDownloader 任务实例
         * @param resultCode 加载结果码
         */
        public void onPrepared(FileDownloader fileDownloader, int resultCode);

        /**
         * 下载进度更新
         * 
         * @param fileDownloader 任务实例
         * @param downSize 总共已下载大小
         */
        public void onProgressUpdate(FileDownloader fileDownloader, long downSize);

        /**
         * 下载完成
         * 
         * @param fileDownloader 任务实例
         */
        public void onCompletion(FileDownloader fileDownloader);

        /**
         * 下载异常，发生在下载开始后，各个下载线程都出现异常，且次数超过最大重试数
         * 
         * @param fileDownloader 任务实例
         * @param errorCode 异常码
         */
        public void onError(FileDownloader fileDownloader, int errorCode);

        /**
         * 判断wifi是否有连接,无连接则暂停下载,并提示用户连接wifi
         */
        public void onWifiConnect();
    }

    /**
     * 用指定的URL创建一个HttpURLConnection
     * 
     * @param url 指定URL
     * @throws IOException 用URL打开一个连接出错
     */
    static HttpURLConnection createHttpConnection(URL url) throws IOException {
        return createHttpConnection(url, -1, 0);
    }

    /**
     * 用指定的URL、起始位置和终点位置创建一个HttpURLConnection
     * 
     * @param url 指定URL
     * @param startByte 起始位置
     * @param endByte 终点位置
     * @throws IOException 用URL打开一个连接出错
     */
    static HttpURLConnection createHttpConnection(URL url, long startByte, long endByte)
            throws IOException {
        //startByte、endByte都是有效值的情况下，startByte不能大于endByte
        if (startByte >= 0 && endByte > 0 && startByte > endByte) {
            throw new IllegalArgumentException(EXCEPTION_MESSAGE_START_GREATER_THAN_END);
        }
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setReadTimeout(TIMEOUT_MILLIS);
        conn.setConnectTimeout(TIMEOUT_MILLIS);
        conn.setRequestProperty(
                "Accept",
                "image/pjpeg, image/pjpeg, image/pjpeg, image/gif, application/x-shockwave-flash, application/x-ms-xbap, image/jpeg,application/xaml+xml, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
        conn.setRequestProperty("Referer", url.toString());
        if (startByte >= 0) {
            /*
             * 指定下载位置
             * field:Range
             * value:bytes=start-end 
             * 获取到的数据，包括start，也包括end；end为空则表示从start到文件末尾
             */
            conn.setRequestProperty("Range", "bytes=" + startByte + "-"
                    + (endByte <= 0 ? "" : endByte));//endByte>0为有效值
            conn.setRequestProperty("Connection", "Keep-Alive");
        }
        return conn;
    }
}
