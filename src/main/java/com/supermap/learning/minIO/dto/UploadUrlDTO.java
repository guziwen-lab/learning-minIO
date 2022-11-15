package com.supermap.learning.minIO.dto;

import lombok.Data;

/**
 * 批量获取上传连接
 *
 * @author lty
 */
@Data
public class UploadUrlDTO {

    private String fileName;

    private String md5;

    private Integer chunkNum;

}
