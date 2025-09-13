package com.gengzi.sftp.nio;

import com.gengzi.sftp.nio.constans.Constants;
import com.gengzi.sftp.s3.client.S3ClientNameEnum;
import software.amazon.awssdk.services.s3.internal.BucketUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


/**
 * 存放配置s3sftp配置项
 *
 *
 */
public class S3SftpNioSpiConfiguration extends HashMap<String, Object> {

    public static final String ENDPOINT = "s3sftp.endpoint";
    public static final String ACCESS_KEY = "s3sftp.accessKey";
    public static final String SECRET_KEY = "s3sftp.secretKey";
    public static final String REGION = "s3sftp.region";

    // 默认超时时间
    public static final String TIME_OUT = "s3sftp.timeout";
    public static Long TIME_OUT_VAL = 60L;
    public static final String TIME_OUT_UNIT = "s3sftp.timeoutUnit";
    public static TimeUnit TIME_OUT_UNIT_VAL = TimeUnit.MILLISECONDS;

    // env配置项
    // 默认用户根目录
    public static final String USER_ROOT_PATH = "s3sftp.userRootPath";
    public static final String USER_ROOT_PATH_DEFAULT_VAL = Constants.PATH_SEPARATOR;
    // 默认客户端实现
    public static final String CLIENT_NAME = "s3sftp.clientName";
    public static final S3ClientNameEnum CLIENT_NAME_DEFAULT_VAL = S3ClientNameEnum.DEFAULT_AWS_S3;


    private static final Pattern ENDPOINT_REGEXP = Pattern.compile("(\\w[\\w\\-\\.]*)?(:(\\d+))?");

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

    public String userRootPath() {
        return get(USER_ROOT_PATH).toString();
    }
}
