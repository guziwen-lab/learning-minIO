package com.supermap.learning.minIO.service.impl;

import com.supermap.learning.minIO.entity.FileInfoEntity;
import com.supermap.learning.minIO.repository.FileInfoRepository;
import com.supermap.learning.minIO.service.FileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author lty
 */
@Service
public class FileInfoServiceImpl implements FileInfoService {

    @Autowired
    private FileInfoRepository fileInfoRepository;

    @Override
    public FileInfoEntity getOne(Long id) {
        return fileInfoRepository.getOne(id);
    }

    @Override
    public void save(FileInfoEntity fileInfoEntity) {
        fileInfoRepository.save(fileInfoEntity);
    }

    @Override
    public FileInfoEntity findByBucketAndObjectName(String bucket, String objectName) {
        return fileInfoRepository.findByBucketAndObjectName(bucket, objectName);
    }

    @Override
    public void deleteById(Long id) {
        fileInfoRepository.deleteById(id);
    }

}
