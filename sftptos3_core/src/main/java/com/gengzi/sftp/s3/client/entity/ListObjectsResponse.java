package com.gengzi.sftp.s3.client.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 目录下包含的文件或者目录属性信息
 */
public class ListObjectsResponse {


    /**
     * 文件路径：文件信息(包含零字节对象)
     */
    private Map<String, ObjectHeadResponse> objects;


    /**
     * 目录路径：子目录信息
     */
    private Map<String, ObjectHeadResponse> prefixes;


    public ListObjectsResponse(Map<String, ObjectHeadResponse> objects, Map<String, ObjectHeadResponse> prefixes) {
        this.objects = objects;
        this.prefixes = prefixes;
    }

    /**
     * 仅获取文件路径名称
     *
     * @return
     */
    public List<String> getObjectsNames() {
        ArrayList<String> names = new ArrayList<>();
        Set<String> fileNames = objects.keySet();
        Set<String> dirNames = prefixes.keySet();
        if (fileNames != null && fileNames.size() > 0) {
            names.addAll(fileNames);
        }
        if (dirNames != null && dirNames.size() > 0) {
            names.addAll(dirNames);
        }
        return names;
    }

    public Map<String, ObjectHeadResponse> getObjects() {
        return objects;
    }

    public Map<String, ObjectHeadResponse> getPrefixes() {
        return prefixes;
    }
}
