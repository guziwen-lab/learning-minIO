package com.supermap.learning.minIO.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author lty
 */
@ConfigurationProperties(prefix = "base.dir")
@Component
@Data
public class BaseFileDirectoryConfigurationProperties {

    private String resource;

    private String uncompress;

}
