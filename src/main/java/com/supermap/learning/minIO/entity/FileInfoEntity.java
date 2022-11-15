package com.supermap.learning.minIO.entity;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "tb_file_info")
@Accessors(chain = true)
public class FileInfoEntity {

    @Id
    private Long id;

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
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

}

