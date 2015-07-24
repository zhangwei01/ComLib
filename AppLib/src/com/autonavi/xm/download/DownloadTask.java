package com.autonavi.xm.download;

import java.io.Serializable;

/**
 * 下载条目
 * 
 * @author if
 * 
 */
public class DownloadTask implements Serializable {

    private static final long serialVersionUID = -4263021281364464912L;

    /**
     * 条目唯一ID
     */
    public int id;

    /**
     * 别名
     */
    public String aliasName;

    /**
     * 下载url
     */
    public String sourceUrl;

    /**
     * 文件保存目录
     */
    public String saveDirPath;

    /**
     * 文件保存路径
     */
    public String saveFilePath;

    /**
     * 已下载大小
     */
    public long downloadedSize;

    /**
     * 文件总大小
     */
    public long totalSize;

    /**
     * 标签，用来储存额外信息，必须是可序列化的对象
     */
    public Serializable tag;

    public DownloadTask(int id, String aliasName, String sourceUrl, String saveDirPath,
            long totalSize) {
        this(id, aliasName, sourceUrl, saveDirPath, totalSize, null);
    }

    public DownloadTask(int id, String aliasName, String sourceUrl, String saveDirPath,
            long totalSize, Serializable tag) {
        this.id = id;
        this.aliasName = aliasName;
        this.sourceUrl = sourceUrl;
        this.saveDirPath = saveDirPath;
        this.totalSize = totalSize;
        this.tag = tag;
    }

}
