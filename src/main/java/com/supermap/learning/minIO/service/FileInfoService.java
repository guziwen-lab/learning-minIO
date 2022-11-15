package com.supermap.learning.minIO.service;

import com.supermap.learning.minIO.entity.FileInfoEntity;

/**
 * @author lty
 */
public interface FileInfoService {

    FileInfoEntity getOne(Long id);

    void save(FileInfoEntity fileInfoEntity);

    FileInfoEntity findByBucketAndObjectName(String bucket, String objectName);

    void deleteById(Long id);
}
