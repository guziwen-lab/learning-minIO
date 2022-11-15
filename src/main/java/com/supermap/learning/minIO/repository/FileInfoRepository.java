package com.supermap.learning.minIO.repository;

import com.supermap.learning.minIO.entity.FileInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author lty
 */
public interface FileInfoRepository extends JpaRepository<FileInfoEntity, Long>, JpaSpecificationExecutor<FileInfoEntity> {

    FileInfoEntity findByBucketAndObjectName(String bucket, String objectName);

}
