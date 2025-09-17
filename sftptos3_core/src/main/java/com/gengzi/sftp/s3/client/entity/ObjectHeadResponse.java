package com.gengzi.sftp.s3.client.entity;


import java.nio.file.attribute.FileTime;
import java.util.List;

/**
 * 对象元信息响应类
 */
public class ObjectHeadResponse {


    private FileTime lastModifiedTime;
    private Long size;
    private Object eTag;
    private boolean isDirectory;
    private boolean isRegularFile;
    /**
     * 是否为空目录，目录下没有任何文件和子目录
     * 如果是文件类型，可以默认返回false
     */
    private boolean isEmptyDirectory = false;

    /**
     * 当前目录下所有文件或者子目录名称
     * 当前为空目录或者为文件时，返回null
     * 如果不为空目录，返回目录下的所有文件或者子目录名称（跟缓存优化有关）
     * 当然也允许不返回，返回null
     */
    private List<String> directoryContentsNames = null;

    public ObjectHeadResponse(FileTime lastModifiedTime,
                              Long size,
                              Object eTag,
                              boolean isDirectory,
                              boolean isRegularFile,
                              boolean isEmptyDirectory,
                              List<String> directoryContentsNames) {
        this.lastModifiedTime = lastModifiedTime;
        this.size = size;
        this.eTag = eTag;
        this.isDirectory = isDirectory;
        this.isRegularFile = isRegularFile;
        this.isEmptyDirectory = isEmptyDirectory;
        this.directoryContentsNames = directoryContentsNames;
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

    public boolean isEmptyDirectory() {
        return isEmptyDirectory;
    }

    public List<String> getDirectoryContentsNames() {
        return directoryContentsNames;
    }
}
