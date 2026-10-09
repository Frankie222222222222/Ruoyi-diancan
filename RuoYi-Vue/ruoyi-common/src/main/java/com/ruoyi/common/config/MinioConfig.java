package com.ruoyi.common.config;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 对象存储配置
 *
 * <p>对应 application.yml 中 {@code minio.*} 配置项。
 * 通过 {@link MinioClient} Bean 注入到 {@code MinioUtils} 使用。
 *
 * @author ruoyi
 */
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    /** S3 API 地址（例：{@code http://127.0.0.1:9000}） */
    private String endpoint;

    /** 访问密钥（Access Key） */
    private String accessKey;

    /** 秘密密钥（Secret Key） */
    private String secretKey;

    /** 业务桶名（启动时会自动创建） */
    private String bucket;

    /** 公开访问前缀（用于拼接图片 URL，浏览器直接访问） */
    private String urlPrefix;

    /** 上传子路径前缀（用于在桶内分模块组织） */
    private String pathPrefix;

    /**
     * 注册 MinIO 客户端为 Spring Bean。
     * 整个应用共享一个客户端，内部维护连接池。
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    public void setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }
}
