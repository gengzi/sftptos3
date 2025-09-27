package com.gengzi.sftp.nio;

import com.gengzi.sftp.nio.constans.Constants;
import com.gengzi.sftp.s3.client.S3ClientNameEnum;
import org.apache.sshd.common.session.SessionContext;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.internal.BucketUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


/**
 * 存放配置s3sftp配置项
 */
public class S3SftpNioSpiConfiguration extends HashMap<String, Object> {

    public static final String ENDPOINT = "s3sftp.endpoint";
    public static final String ACCESS_KEY = "s3sftp.accessKey";
    public static final String SECRET_KEY = "s3sftp.secretKey";
    public static final String REGION = "s3sftp.region";
    public static final Region REGION_VAL = Region.US_EAST_1;
    // 下载文件时分片大小
    public static final String FILE_MAXFRAGMENTSIZE = "s3sftp.fileMaxFragmentSize";
    // 默认64kb
    public static final int FILE_MAXFRAGMENTSIZE_VAL = 64 * 1024;
    public static final String FILE_MAXNUMBERFRAGMENTS = "s3sftp.fileMaxNumberFragments";
    public static final int FILE_MAXNUMBERFRAGMENTS_VAL = 30;


    // 默认超时时间
    public static final String TIME_OUT = "s3sftp.timeout";
    public static final String TIME_OUT_UNIT = "s3sftp.timeoutUnit";
    // env配置项
    // 默认用户根目录
    public static final String USER_ROOT_PATH = "s3sftp.userRootPath";
    public static final String USER_ROOT_PATH_DEFAULT_VAL = "";
    // 默认客户端实现
    public static final String CLIENT_NAME = "s3sftp.clientName";
    public static final S3ClientNameEnum CLIENT_NAME_DEFAULT_VAL = S3ClientNameEnum.DEFAULT_AWS_S3;
    // sessionContext
    public static final String SESSION_CONTEXT = "s3sftp.sessionContext";
    private static final Pattern ENDPOINT_REGEXP = Pattern.compile("(\\w[\\w\\-\\.]*)?(:(\\d+))?");
    private static final String USER_PATH_FILE_ATTRIBUTES_CACHE_KEY_PREFIX = "s3sftp.attribute.k.";
    private static final String DIRECTORY_CONTENTS_NAMES_CACHE_KEY_PREFIX = "s3sftp.directory.k.";
    public static Long TIME_OUT_VAL = 5 * 60L;
    public static TimeUnit TIME_OUT_UNIT_VAL = TimeUnit.SECONDS;
    // 桶
    private String bucketName;


    public S3SftpNioSpiConfiguration() {
        this(new HashMap<>());

    }

    public S3SftpNioSpiConfiguration(Map<String, ?> env) {
        Objects.requireNonNull(env);
        // setup defaults
        put(TIME_OUT, TIME_OUT_VAL);
        put(TIME_OUT_UNIT, TIME_OUT_UNIT_VAL);
        put(CLIENT_NAME, CLIENT_NAME_DEFAULT_VAL);
        put(USER_ROOT_PATH, USER_ROOT_PATH_DEFAULT_VAL);
        put(REGION, REGION_VAL);
        put(FILE_MAXFRAGMENTSIZE, FILE_MAXFRAGMENTSIZE_VAL);
        put(FILE_MAXNUMBERFRAGMENTS, FILE_MAXNUMBERFRAGMENTS_VAL);


        // 覆盖默认配置
        env.keySet().forEach(key -> put(key, env.get(key)));

    }

    public String getBucketName() {
        return bucketName;
    }


    public S3SftpNioSpiConfiguration withEndpoint(String endpoint) {
        Objects.requireNonNull(endpoint);
        if (!ENDPOINT_REGEXP.matcher(endpoint).matches()) {
            throw new IllegalArgumentException(
                    String.format("endpoint '%s' does not match format host:port where port is a number", endpoint));
        }
        this.put(ENDPOINT, endpoint);
        return this;
    }

    public S3SftpNioSpiConfiguration withBucketName(String bucket) {
        if (bucket != null) {
            BucketUtils.isValidDnsBucketName(bucket, true);
        }
        this.bucketName = bucket;
        return this;
    }

    public S3SftpNioSpiConfiguration withCredentials(String accessKey, String secretAccessKey) {
        if (Objects.isNull(accessKey) || Objects.isNull(secretAccessKey)) {
            throw new IllegalArgumentException("accessKey or secretAccessKey can not be null");
        }
        put(ACCESS_KEY, accessKey);
        put(SECRET_KEY, secretAccessKey);
        return this;
    }


    public String getEndpoint() {
        return get(ENDPOINT).toString();
    }

    public URI endpointUri() {
        return URI.create("http://" + get(ENDPOINT).toString());
    }

    public String accessKey() {
        return get(ACCESS_KEY).toString();
    }

    public String secretKey() {
        return get(SECRET_KEY).toString();
    }

    public Long timeout() {
        return (Long) get(TIME_OUT);
    }

    public TimeUnit timeoutUnit() {
        return (TimeUnit) get(TIME_OUT_UNIT);
    }


    public S3ClientNameEnum clientName() {
        return (S3ClientNameEnum) get(CLIENT_NAME);
    }

    /**
     * 兼容根目录为 '\' '/' 的情况，都认为是用户根目录为整个对象存储下的
     *
     * @return
     */
    public String userRootPath() {
        String root = get(USER_ROOT_PATH).toString();
        if ("\\".equals(root) || Constants.PATH_SEPARATOR.equals(root)) {
            return "";
        }
        return root;
    }

    public SessionContext sessionContext() {
        return (SessionContext) get(SESSION_CONTEXT);
    }


    public String region() {
        return get(REGION).toString();
    }

    /**
     * 返回UserPathFileAttributesCacheKey
     * 拼装规则
     * username:endpoint/bucketName/pathkey
     *
     * @return
     */
    public String getUserPathFileAttributesCacheKey(String pathKey) {
        String username = sessionContext().getUsername();
        String cacheKeyPrefixFormat = USER_PATH_FILE_ATTRIBUTES_CACHE_KEY_PREFIX + "%s:%s/%s/%s";
        return String.format(cacheKeyPrefixFormat, username, getEndpoint(), bucketName, pathKey);
    }


    public String getDirectoryContentsNamesCacheKey(String pathKey) {
        String username = sessionContext().getUsername();
        String cacheKeyPrefixFormat = DIRECTORY_CONTENTS_NAMES_CACHE_KEY_PREFIX + "%s:%s/%s/%s";
        return String.format(cacheKeyPrefixFormat, username, getEndpoint(), bucketName, pathKey);
    }


    public String getStroageInfo() {
        String username = sessionContext().getUsername();
        return String.format("%s:%s/%s", username, getEndpoint(), bucketName);
    }


    public int getFileMaxFragmentSize() {
        return (int) get(FILE_MAXFRAGMENTSIZE);
    }

    public int getFileMaxNumberFragments() {
        return (int) get(FILE_MAXNUMBERFRAGMENTS);
    }
}
