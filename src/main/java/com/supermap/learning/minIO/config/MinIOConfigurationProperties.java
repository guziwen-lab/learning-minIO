package com.supermap.learning.minIO.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author lty
 */
@ConfigurationProperties(prefix = "minio")
@Data
public class MinIOConfigurationProperties {

    private String endpoint;

    private String accessKey;

    private String secretKey;

    /**
     * 获取带签名的临时上传元数据对象的超时时间，单位为天
     */
    private Integer presignedPostFormDataExpiration = 1;

    /**
     * 获取文件url的超时时间，单位为天
     */
    private Integer presignedObjectUrlExpiration = 1;

}
