package com.gengzi.sftp.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;

public class ResourceUtils {

    public static File readResource(String path) throws IOException {
        // 构造资源对象：classpath: 可省略（默认就是类路径）
        Resource resource = new ClassPathResource(path);
        if (resource.exists()) {
            return resource.getFile();
        }
        return null;
    }
}