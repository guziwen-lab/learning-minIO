package com.supermap.learning.minIO.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author lty
 */
@Data
@Accessors(chain = true)
public class MinIOComposeDTO {

    /**
     * 桶名称
     */
    private String bucket;

    /**
     * 对象名称
     */
    private String object;

}
