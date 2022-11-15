package com.supermap.learning.minIO.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传状态
 *
 * @author lty
 */
@Data
public class FileStateVO {

    /**
     * 是否上传过
     */
    private Boolean isUploaded;

    /**
     * 已收到的分片序号
     */
    private List<String> chunkIndex = new ArrayList<>();

}
