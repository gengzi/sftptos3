package com.gengzi.sftp.s3.client.entity;


import java.nio.file.attribute.FileTime;

/**
 * 对象元信息响应类
 *
 * @author gengzi
 */
public class ObjectHeadResponse {


    /**
     * 文件最后修改时间
     */
    private FileTime lastModifiedTime;
    /**
     * 文件大小
     */
    private Long size;
    /**
     * 文件ETag
     */
    private Object eTag;
    /**
     * 是否为目录
     */
    private boolean isDirectory;

    /**
     * 是否为常规文件（非目录、符号链接、设备文件等特殊类型）
     */
    private boolean isRegularFile;


    /**
     * 缓存优化相关：
     * <p>
     * 当前目录下所有文件或者子目录名称
     * 当前为空目录或者为文件时，返回 null即可
     * 如果不为空目录，返回目录下的所有文件或者子目录名称
     * 当然也允许不返回，返回 null即可
     */
    private ListObjectsResponse listObjects = null;

    public ObjectHeadResponse(FileTime lastModifiedTime,
                              Long size,
                              Object eTag,
                              boolean isDirectory,
                              boolean isRegularFile,
                              ListObjectsResponse listObjects) {
        this.lastModifiedTime = lastModifiedTime;
        this.size = size;
        this.eTag = eTag;
        this.isDirectory = isDirectory;
        this.isRegularFile = isRegularFile;
        this.listObjects = listObjects;
    }

    /**
     * 不返回 listObjects
     *
     * @param lastModifiedTime
     * @param size
     * @param eTag
     * @param isDirectory
     * @param isRegularFile
     */
    public ObjectHeadResponse(FileTime lastModifiedTime,
                              Long size,
                              Object eTag,
                              boolean isDirectory,
                              boolean isRegularFile) {
        this.lastModifiedTime = lastModifiedTime;
        this.size = size;
        this.eTag = eTag;
        this.isDirectory = isDirectory;
        this.isRegularFile = isRegularFile;
    }


    public FileTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public Long getSize() {
        return size;
    }

    public Object geteTag() {
        return eTag;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isRegularFile() {
        return isRegularFile;
    }

    public ListObjectsResponse getListObjects() {
        return listObjects;
    }


}
