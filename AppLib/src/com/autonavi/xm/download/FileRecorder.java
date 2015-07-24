package com.autonavi.xm.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * <pre>
 * 
 * 文件结构：
 * threadNum|totalSize|downloadSize|downloadSize|...|targetUrl
 *    int   |  long   |    long    |    long    |...| String
 * 
 * </pre>
 * 
 * @author i.F
 * 
 */
public class FileRecorder {

    /**
     * 记录文件的后缀
     */
    private static String LOG_FILE_SUFFIX = ".gddl";

    private final String EXCEPTION_MESSAGE_ILLEGAL_SAVE_STATE = "Number of threads must be saved first!";

    private final int INTEGER_BYTES_NUM = Integer.SIZE / Byte.SIZE;
    private final int LONG_BYTES_NUM = Long.SIZE / Byte.SIZE;

    private final File mRecordFile;
    private RandomAccessFile mRecordAccessFile;

    /**
     * 下载线程数
     */
    private int mThreadNum;

    public FileRecorder(String saveFilePath, boolean fromRecord) {
        this(new File(saveFilePath), fromRecord);
    }

    public FileRecorder(File saveFile, boolean fromRecord) {
        mRecordFile = saveFile != null ? new File(saveFile.getAbsolutePath()
                .concat(LOG_FILE_SUFFIX)) : null;
        if (!fromRecord) {
            removeRecordFile();
        }
        try {
            mRecordAccessFile = new RandomAccessFile(mRecordFile, "rw");
        } catch (FileNotFoundException e) {
        }
        mThreadNum = readThreadNum();
    }

    /**
     * 保存下载总大小
     * 
     * @param totalSize
     *            下载总大小
     */
    public synchronized void saveTotalSize(long totalSize) {
        if (readTotalSize() > 0) {
            return;
        }
        RandomAccessFile recordFile = mRecordAccessFile;
        if (recordFile == null) {
            return;
        }
        try {
            recordFile.seek(INTEGER_BYTES_NUM);
            recordFile.writeLong(totalSize);
        } catch (IOException e) {
        }
    }

    /**
     * 读取下载总大小
     * 
     * @return 下载总大小
     */
    public synchronized long readTotalSize() {
        if (!mRecordFile.exists()) {
            return 0;
        }
        RandomAccessFile recordFile = mRecordAccessFile;
        if (recordFile == null) {
            return 0;
        }
        long totalSize = 0;
        try {
            recordFile.seek(INTEGER_BYTES_NUM);
            totalSize = recordFile.readLong();
        } catch (IOException e) {
        }
        return totalSize;
    }

    /**
     * 保存下载线程数。<b>必须在其他save操作之前调用</b>
     * 
     * @param threadNum
     *            线程数
     */
    public synchronized void saveThreadNum(int threadNum) {
        if (readThreadNum() > 0) {
            return;
        }
        RandomAccessFile recordFile = mRecordAccessFile;
        if (recordFile == null) {
            return;
        }
        try {
            recordFile.seek(0);
            recordFile.writeInt(threadNum);
            mThreadNum = threadNum;
        } catch (IOException e) {
        }
    }

    /**
     * 读取下载线程数
     * 
     * @return 线程数
     */
    public synchronized int readThreadNum() {
        if (!mRecordFile.exists()) {
            return 0;
        }
        RandomAccessFile recordFile = mRecordAccessFile;
        if (recordFile == null) {
            return 0;
        }
        int threadNum = 0;
        try {
            recordFile.seek(0);
            threadNum = recordFile.readInt();
        } catch (IOException e) {
        }
        return threadNum;
    }

    /**
     * 保存下载URL。如果是首次保存，必须在saveThreadNum之后调用，否则将抛出IllegalStateException
     * 
     * @param url
     *            下载URL
     * @throws IllegalStateException
     *             如果是首次保存并且尚未保存线程数
     */
    public synchronized void saveDownloadUrl(String url) {
        if (mThreadNum <= 0) {
            //TODO throw exception or just return?
            //throw new IllegalStateException(EXCEPTION_MESSAGE_ILLEGAL_SAVE_STATE);
            return;
        }
        RandomAccessFile recordFile = mRecordAccessFile;
        if (recordFile == null) {
            return;
        }
        try {
            recordFile.seek(INTEGER_BYTES_NUM + LONG_BYTES_NUM + LONG_BYTES_NUM * mThreadNum);
            recordFile.writeUTF(url);
        } catch (IOException e) {
        }
    }

    /**
     * 读取下载URL
     * 
     * @return 下载URL
     */
    public synchronized String readDownloadUrl() {
        if (!mRecordFile.exists() || mThreadNum <= 0) {
            return null;
        }
        RandomAccessFile recordFile = mRecordAccessFile;
        if (recordFile == null) {
            return null;
        }
        String url = null;
        try {
            recordFile.seek(INTEGER_BYTES_NUM + LONG_BYTES_NUM + LONG_BYTES_NUM * mThreadNum);
            url = recordFile.readUTF();
        } catch (IOException e) {
        }
        return url;
    }

    /**
     * 保存各个线程已下载大小
     * 
     * @param threadId
     *            线程ID，目前设定为线程索引
     * @param downSize
     *            已下载大小
     */
    public synchronized void saveDownloadSize(int threadId, long downSize) {
        RandomAccessFile recordFile = mRecordAccessFile;
        if (recordFile == null) {
            return;
        }
        try {
            recordFile.seek(INTEGER_BYTES_NUM + LONG_BYTES_NUM + LONG_BYTES_NUM * threadId);
            recordFile.writeLong(downSize);
        } catch (IOException e) {
        }
    }

    /**
     * 读取某个线程已下载大小
     * 
     * @param threadId
     *            线程ID，目前设定为线程索引
     * @return 已下载大小
     */
    public synchronized long readDownloadSize(int threadId) {
        if (!mRecordFile.exists()) {
            return 0;
        }
        RandomAccessFile recordFile = mRecordAccessFile;
        if (recordFile == null) {
            return 0;
        }
        long size = 0;
        try {
            recordFile.seek(INTEGER_BYTES_NUM + LONG_BYTES_NUM + LONG_BYTES_NUM * threadId);
            size = recordFile.readLong();
        } catch (IOException e) {
        }
        return size;
    }

    /**
     * 读取总下载大小
     * 
     * @return 总下载大小
     */
    public synchronized long readTotalDownloadSize() {
        if (!mRecordFile.exists()) {
            return 0;
        }
        long size = 0;
        int threadNum = readThreadNum();
        for (int i = 0; i < threadNum; i++) {
            size += readDownloadSize(i);
        }
        return size;
    }

    /**
     * 删除记录文件，下载完成时调用
     */
    public void removeRecordFile() {
        if (mRecordFile == null || !mRecordFile.exists()) {
            return;
        }
        mRecordFile.delete();
    }

    /**
     * 检查是否为记录文件
     * 
     * @param filePath
     *            待检查的文件路径
     * @return 如果文件为记录文件则返回true，否则为false
     */
    public static boolean isRecordFile(String filePath) {
        if (filePath == null) {
            return false;
        }
        return filePath.endsWith(LOG_FILE_SUFFIX);
    }

    /**
     * 检查是否为记录文件
     * 
     * @param file
     *            待检查的文件
     * @return 如果文件为记录文件则返回true，否则为false
     */
    public static boolean isRecordFile(File file) {
        return isRecordFile(file.getAbsolutePath());
    }
}
