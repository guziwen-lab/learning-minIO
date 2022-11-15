package com.supermap.learning.minIO.vo;

import lombok.Data;

import java.util.Map;

/**
 * @author lty
 */
@Data
public class UploadUrlVO {

    private Integer index;

    private Map<String, String> formData;

    private String url;

}
