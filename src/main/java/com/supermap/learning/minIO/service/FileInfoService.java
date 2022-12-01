package com.supermap.learning.minIO.service;

import com.supermap.learning.minIO.entity.FileInfoEntity;
import org.springframework.lang.Nullable;

/**
 * @author lty
 */
public interface FileInfoService {

    FileInfoEntity getOne(Long id);

    void save(FileInfoEntity fileInfoEntity);

    FileInfoEntity findByBucketAndObjectName(String bucket, String objectName);

    void deleteById(Long id);

    void save(String bucket, String objectName, @Nullable String md5, String fileName, String path);

}
