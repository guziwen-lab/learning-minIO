package com.supermap.learning.minIO.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 文件第一次上传，创建该状态文件到分片目录下
 *
 * @author lty
 */
@Data
@Accessors(chain = true)
public class FileStateBO {

    /**
     * 桶名称
     */
    private String bucket;

    /**
     * 带路径的文件名称
     */
    private String objectName;

    /**
     * 路径
     */
    private String path;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * MD5值
     */
    private String md5;

    /**
     * 分片数量
     */
    private Integer chunkNum;

}
