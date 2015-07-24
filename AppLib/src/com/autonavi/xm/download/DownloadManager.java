
package com.autonavi.xm.download;

import com.autonavi.xm.download.DownloadInterface.OnCompletionListener;
import com.autonavi.xm.download.DownloadInterface.OnErrorListener;
import com.autonavi.xm.download.DownloadInterface.OnPreparedListener;
import com.autonavi.xm.download.DownloadInterface.OnProgressUpdateListener;
import com.autonavi.xm.download.DownloadInterface.OnStartedListener;
import com.autonavi.xm.download.DownloadInterface.OnStoppedListener;
import com.autonavi.xm.download.DownloadInterface.OnWifiConnectListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;

/**
 * 下载管理器。提供添加、删除下载任务等操作，每个下载任务都会分配一个唯一的ID。
 * 
 * @author if
 */
public class DownloadManager implements FileDownloader.Callback {

    /**
     * 无效的任务ID，添加任务失败时返回该值
     */
    public static final int INVALID_TASK_ID = 0;

    /**
     * 最小的通知字节数，如果启用
     */
    private static final long MIN_NOTIFY_DOWNLOAD_SIZE = 200 * 1024;//200kb

    /**
     * 任务队列的储存File
     */
    private final File mStoreFile;

    /**
     * 任务ID -> DownloadTask 映射表，用于持久化保存下载队列
     */
    private LinkedHashMap<Integer, DownloadTask> mTaskMap;

    /**
     * 任务ID -> FileDownloader 映射表，方便通过ID控制下载任务
     */
    private final Hashtable<Integer, FileDownloader> mDownloaderMap;

    /**
     * 是否启用下载进度延迟通知，目前默认启用，尚无提供设置接口。延迟通知：下载进度改变时并不马上通知回调而是等积累下载到设定大小时再执行回调通知。
     */
    private final boolean mLazyProgressNotifyEnabled = true;

    private OnStartedListener mOnStartedListener;

    private OnStoppedListener mOnStoppedListener;

    private OnPreparedListener mOnPreparedListener;

    private OnProgressUpdateListener mOnProgressUpdateListener;

    private OnCompletionListener mOnCompletionListener;

    private OnErrorListener mOnErrorListener;

    private OnWifiConnectListener mOnWifiConnectListener;

    /**
     * 指定队列保存路径初始化下载管理器
     * 
     * @param storeFilePath 队列保存路径
     */
    public DownloadManager(String storeFilePath) {
        this(new File(storeFilePath));
    }

    /**
     * 指定队列保存文件初始化下载管理器
     * 
     * @param storeFile 队列保存文件
     */
    public DownloadManager(File storeFile) {
        mStoreFile = storeFile;
        mDownloaderMap = new Hashtable<Integer, FileDownloader>();
        if (storeFile != null && storeFile.exists()) {
            load();
        }
        if (mTaskMap == null) {
            mTaskMap = new LinkedHashMap<Integer, DownloadTask>();
        }
    }

    /**
     * 设置任务已启动的监听器
     * 
     * @param listener 监听器
     */
    public void setOnStartedListener(OnStartedListener listener) {
        mOnStartedListener = listener;
    }

    /**
     * 设置任务已停止的监听器
     * 
     * @param listener 监听器
     */
    public void setOnStoppedListener(OnStoppedListener listener) {
        mOnStoppedListener = listener;
    }

    /**
     * 设置任务已准备的监听器
     * 
     * @param listener 监听器
     */
    public void setOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    /**
     * 设置下载进度已更新的监听器
     * 
     * @param listener 监听器
     */
    public void setOnProgressUpdateListener(OnProgressUpdateListener listener) {
        mOnProgressUpdateListener = listener;
    }

    /**
     * 设置任务已完成的监听器
     * 
     * @param listener 监听器
     */
    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    /**
     * 设置任务出错的监听器
     * 
     * @param listener 监听器
     */
    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    /**
     * 设置下载时网络断开的监听器
     * 
     * @param listener 监听器
     */
    public void setOnWifiConnectListener(OnWifiConnectListener listener) {
        mOnWifiConnectListener = listener;
    }

    /**
     * 生成任务ID。利用UUID.randomUUID().hashCode()生成随机的唯一ID
     * 
     * @return 生成的ID
     */
    private int genTaskID() {
        return UUID.randomUUID().hashCode();
    }

    /**
     * 添加下载任务
     * 
     * @param aliasName 任务别名
     * @param sourceUrl 下载URL
     * @param saveDirPath 保存路径
     * @param supposeSize 下载文件的预定大小
     * @return 生成的任务ID
     */
    public synchronized int addTask(String aliasName, String sourceUrl, String saveDirPath,
            long supposeSize) {
        return addTask(aliasName, sourceUrl, saveDirPath, supposeSize, null);
    }

    /**
     * 添加下载任务
     * 
     * @param aliasName 任务别名
     * @param sourceUrl 下载URL
     * @param saveDirPath 保存路径
     * @param supposeSize 下载文件的预定大小
     * @param tag 任务标签
     * @return 生成的任务ID
     */
    public synchronized int addTask(String aliasName, String sourceUrl, String saveDirPath,
            long supposeSize, Serializable tag) {
        //生成任务ID，向任务队列添加 DownloadTask ，添加FileDownloader
        int id = genTaskID();
        DownloadTask item = new DownloadTask(id, aliasName, sourceUrl, saveDirPath, supposeSize,
                tag);
        mTaskMap.put(id, item);
        addDownloader(id, sourceUrl, saveDirPath);
        return id;
    }

    /**
     * 在指定索引位置插入一个任务
     * 
     * @param aliasName 任务别名
     * @param sourceUrl 下载URL
     * @param saveDirPath 保存路径
     * @param supposeSize 下载文件的预定大小
     * @param index 要插入的索引位置，小于零或大于队列总个数则默认添加到队列末尾
     * @return 生成的任务ID
     */
    public synchronized int insertTask(String aliasName, String sourceUrl, String saveDirPath,
            int supposeSize, int index) {
        return insertTask(aliasName, sourceUrl, saveDirPath, supposeSize, null, index);
    }

    /**
     * 在指定索引位置插入一个任务
     * 
     * @param aliasName 任务别名
     * @param sourceUrl 下载URL
     * @param saveDirPath 保存路径
     * @param supposeSize 下载文件的预定大小
     * @param tag 任务标签
     * @param index 要插入的索引位置，小于零或大于队列总个数则默认添加到队列末尾
     * @return 生成的任务ID
     */
    @SuppressWarnings("unchecked")
    public synchronized int insertTask(String aliasName, String sourceUrl, String saveDirPath,
            int supposeSize, Serializable tag, int index) {
        int id = genTaskID();
        DownloadTask item = new DownloadTask(id, aliasName, sourceUrl, saveDirPath, supposeSize,
                tag);
        LinkedHashMap<Integer, DownloadTask> nItemMap = mTaskMap;
        //索引位置小于零或大于队列总个数则默认添加到队列末尾
        if (index < 0 || index >= nItemMap.size()) {
            nItemMap.put(id, item);
        } else {
            //LinkedHashMap没有插入的方法，只能克隆一份，将原来的队列清空，重新按顺序添加回去，到需要插入的索引位置时将新的DownloadTask添加进去
            LinkedHashMap<Integer, DownloadTask> oldItemMap = (LinkedHashMap<Integer, DownloadTask>) nItemMap
                    .clone();
            nItemMap.clear();
            Set<Integer> keySet = oldItemMap.keySet();
            int count = 0;
            for (Integer key : keySet) {
                if (count == index) {
                    nItemMap.put(id, item);
                }
                nItemMap.put(key, oldItemMap.get(key));
                count++;
            }
        }
        addDownloader(id, sourceUrl, saveDirPath);
        return id;
    }

    /**
     * 提交保存。执行任务操作（添加、删除）后不立即保存队列到文件，而是调用该方法后保存。避免批量操作时每次都保存文件造成操作耗时。
     */
    public void commit() {
        store();
    }

    /**
     * 启动制定ID的下载任务
     * 
     * @param id 任务ID
     */
    public void startTask(int id) {
        FileDownloader fd = mDownloaderMap.get(id);
        if (fd != null) {
            fd.start();
        }
    }

    /**
     * 停止制定ID的下载任务
     * 
     * @param id 任务ID
     */
    public void stopTask(int id) {
        FileDownloader fd = mDownloaderMap.get(id);
        if (fd != null) {
            fd.stop();
        }
    }

    /**
     * 删除无用的文件（临时文件等），一般是在删除任务或下载完成时调用
     * 
     * @param id 任务ID
     */
    public void removeUselessFiles(int id) {
        FileDownloader fd = mDownloaderMap.get(id);
        if (fd != null) {
            fd.removeUselessFiles();
        }
    }

    /**
     * 获取指定任务下载文件的总大小
     * 
     * @param id 任务ID
     * @return 下载文件的总大小
     */
    public long getTaskTotalSize(int id) {
        long totalSize = 0;
        FileDownloader fd = mDownloaderMap.get(id);
        if (fd != null) {
            totalSize = fd.getTotalSize();
        }
        if (totalSize == 0) {
            DownloadTask item = getTaskItem(id);
            if (item != null) {
                totalSize = item.totalSize;
            }
        }
        return totalSize;
    }

    /**
     * 获取指定任务的已下载大小
     * 
     * @param id 任务ID
     * @return 已下载大小
     */
    public long getTaskDownloadedSize(int id) {
        FileDownloader fd = mDownloaderMap.get(id);
        if (fd != null) {
            return fd.getDownloadedSize();
        }
        return 0;
    }

    /**
     * 删除指定任务
     * 
     * @param id 任务ID
     * @return 被删除的DownloadTask对象
     */
    public synchronized DownloadTask removeTask(int id) {
        FileDownloader fd = mDownloaderMap.remove(id);
        if (fd != null) {
            fd.stop();
            fd.removeUselessFiles();
        }
        DownloadTask item = mTaskMap.remove(id);
        return item;
    }

    /**
     * 清空任务列表
     */
    public synchronized void clearTasks() {
        shutdown();
        mDownloaderMap.clear();
        mTaskMap.clear();
    }

    /**
     * 停止所有任务
     */
    public void shutdown() {
        Enumeration<Integer> enumeration = mDownloaderMap.keys();
        while (enumeration.hasMoreElements()) {
            int id = enumeration.nextElement();
            stopTask(id);
        }
    }

    /**
     * 获取任务是否已启动
     * 
     * @param id 任务ID
     * @return 任务存在且已启动返回true，否则返回false
     */
    public boolean isTaskStarted(int id) {
        FileDownloader fd = mDownloaderMap.get(id);
        if (fd != null) {
            return fd.isStarted();
        }
        return false;
    }

    /**
     * 获取正在运行的任务数组
     * 
     * @return 正在运行的任务数组，如果没有则返回null
     */
    public synchronized DownloadTask[] getRunningTasks() {
        Collection<FileDownloader> downloaders = mDownloaderMap.values();
        int count = 0;
        ArrayList<DownloadTask> itemList = new ArrayList<DownloadTask>();
        for (FileDownloader fd : downloaders) {
            if (fd.isStarted()) {
                itemList.add(mTaskMap.get(fd.getId()));
                count++;
            }
        }
        if (count > 0) {
            DownloadTask[] items = new DownloadTask[count];
            itemList.toArray(items);
            return items;
        }
        return null;
    }

    /**
     * 获取指定的任务DownloadTask
     * 
     * @param id 任务ID
     * @return 队列中的DownloadTask
     */
    public synchronized DownloadTask getTaskItem(int id) {
        return mTaskMap.get(id);
    }

    /**
     * 获取队列中首个任务
     * 
     * @return 队列中的首个任务，队列为空返回null
     */
    public synchronized DownloadTask getFirstTaskItem() {
        Collection<DownloadTask> itemClc = mTaskMap.values();
        if (itemClc != null && itemClc.size() > 0) {
            return itemClc.iterator().next();
        }
        return null;
    }

    /**
     * 获取所有任务
     * 
     * @return 以数组形式返回所有的任务
     */
    public synchronized DownloadTask[] getTaskItems() {
        Collection<DownloadTask> itemClc = mTaskMap.values();
        DownloadTask[] items = new DownloadTask[itemClc.size()];
        itemClc.toArray(items);
        return items;
    }

    /**
     * 获取队列的任务总个数
     * 
     * @return 队列的任务总个数
     */
    public synchronized int getTaskItemListSize() {
        return mTaskMap.size();
    }

    /**
     * 创建FileDownloader并添加到映射表中。
     * 
     * @param id 任务ID
     * @param sourceUrl 下载源URL
     * @param saveDirPath 文件保存目录
     * @return 创建并添加成功返回true，否则返回false
     */
    private boolean addDownloader(int id, String sourceUrl, String saveDirPath) {
        return addDownloader(id, null, sourceUrl, saveDirPath);
    }

    /**
     * 创建FileDownloader并添加到映射表中。
     * 
     * @param id 任务ID
     * @param saveFilePath 文件保存路径
     * @return 创建并添加成功返回true，否则返回false
     */
    private boolean addDownloader(int id, String saveFilePath) {
        return addDownloader(id, saveFilePath, null, null);
    }

    /**
     * 创建FileDownloader并添加到映射表中。
     * 
     * @param id 任务ID
     * @param saveFilePath 文件保存路径
     * @param sourceUrl 下载源URL
     * @param saveDirPath 文件保存目录
     * @return 创建并添加成功返回true，否则返回false
     */
    private boolean addDownloader(int id, String saveFilePath, String sourceUrl, String saveDirPath) {
        if (saveFilePath == null && (sourceUrl == null || saveDirPath == null)) {
            return false;
        }
        FileDownloader downloader = null;
        //如果指定了下载保存路径则为续传
        if (saveFilePath != null && saveFilePath.length() > 0) {
            downloader = new FileDownloader(new File(saveFilePath), this);
        } else if (sourceUrl != null && saveDirPath != null) {//指定了下载源URL和文件保存目录则为新创建的下载
            try {
                downloader = new FileDownloader(new URL(sourceUrl), saveDirPath, this);
            } catch (MalformedURLException e) {
            }
        }
        if (downloader != null) {
            downloader.setId(id);
            mDownloaderMap.put(id, downloader);
            return true;
        }
        return false;
    }

    /**
     * 保存下载任务列表到文件。
     * 
     * @return 保存成功返回true，否则返回false
     */
    private synchronized boolean store() {
        ObjectOutputStream oos = null;
        try {
            File parent = mStoreFile.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            oos = new ObjectOutputStream(new FileOutputStream(mStoreFile));
            oos.writeObject(mTaskMap);
            oos.flush();
            return true;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }

    /**
     * 从文件读取加载下载任务。根据保存的DownloadTask相关信息创建FileDownloader
     * 
     * @return 加载成功返回true，否则返回false
     */
    @SuppressWarnings("unchecked")
    private boolean load() {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(mStoreFile));
            mTaskMap = (LinkedHashMap<Integer, DownloadTask>) ois.readObject();
            if (mTaskMap.size() > 0) {
                Collection<DownloadTask> items = mTaskMap.values();
                for (DownloadTask item : items) {
                    String saveFilePath = item.saveFilePath;
                    addDownloader(item.id, saveFilePath, item.sourceUrl, item.saveDirPath);
                }
            }
        } catch (StreamCorruptedException e) {
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }

    /**
     * 任务已启动。回调通知
     */
    @Override
    public void onStarted(FileDownloader fileDownloader) {
        int id = fileDownloader.getId();
        if (mOnStartedListener != null) {
            mOnStartedListener.onStarted(id);
        }
    }

    /**
     * 任务已停止。回调通知
     */
    @Override
    public void onStopped(FileDownloader fileDownloader) {
        int id = fileDownloader.getId();
        if (mOnStoppedListener != null) {
            mOnStoppedListener.onStopped(id);
        }
    }

    /**
     * 任务已准备。回调通知
     */
    @Override
    public void onPrepared(FileDownloader fileDownloader, int resultCode) {
        int id = fileDownloader.getId();
        //如果任务准备成功，从FileDownloader获取相关信息设置DownloadTask对象并保存。
        if (resultCode == DownloadInterface.PREPARE_RESULT_CREATE_SUCCESS) {
            DownloadTask item = mTaskMap.get(id);
            if (item != null) {
                File saveFile = fileDownloader.getSaveFile();
                item.saveFilePath = saveFile.getAbsolutePath();
                if (item.aliasName == null) {
                    item.aliasName = saveFile.getName();
                }
                item.totalSize = fileDownloader.getTotalSize();
                store();
            }
        }
        if (mOnPreparedListener != null) {
            mOnPreparedListener.onPrepared(id, resultCode);
        }
    }

    /**
     * 下载进度已更新。回调通知
     */
    @Override
    public void onProgressUpdate(FileDownloader fileDownloader, long downSize) {
        int id = fileDownloader.getId();
        long notifyProgress = -1;
        //如果启用延迟通知，此次下载大小超过设定的最小通知大小MIN_NOTIFY_DOWNLOAD_SIZE时才执行回调
        if (mLazyProgressNotifyEnabled) {
            DownloadTask item = getTaskItem(id);
            if (item != null
                    && (downSize == 0 || downSize - item.downloadedSize > MIN_NOTIFY_DOWNLOAD_SIZE || downSize >= item.totalSize)) {
                notifyProgress = downSize;
                item.downloadedSize = downSize;
            }
        } else {
            notifyProgress = downSize;
        }
        if (notifyProgress >= 0) {
            if (mOnProgressUpdateListener != null) {
                mOnProgressUpdateListener.onProgressUpdate(id, notifyProgress);
            }
        }
    }

    /**
     * 下载已完成。回调通知
     */
    @Override
    public void onCompletion(FileDownloader fileDownloader) {
        int id = fileDownloader.getId();
        if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(id);
        }
    }

    /**
     * 下载出错。回调通知
     */
    @Override
    public void onError(FileDownloader fileDownloader, int errorCode) {
        int id = fileDownloader.getId();
        if (mOnErrorListener != null) {
            mOnErrorListener.onError(id, errorCode);
        }
    }

    /**
     * 下载时网络断开,回调通知
     */
    @Override
    public void onWifiConnect() {
        if (mOnWifiConnectListener != null) {
            mOnWifiConnectListener.onWifiConnect();
        }
    }
}
