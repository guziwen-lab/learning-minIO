package com.supermap.learning.minIO.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "compress.thread")
@Data
public class ThreadPoolConfigurationProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
}
