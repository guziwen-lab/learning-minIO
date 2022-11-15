package com.supermap.learning.minIO.dto;

import lombok.Data;

import java.util.List;

/**
 * @author lty
 */
@Data
public class ComposeDTO {

    private List<MinIOComposeDTO> minIOComposeDTO;

    private String destBucketName;

    private String destObjectName;

}
