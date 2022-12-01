package com.supermap.learning.minIO.service.impl;

import com.supermap.learning.minIO.entity.FileInfoEntity;
import com.supermap.learning.minIO.repository.FileInfoRepository;
import com.supermap.learning.minIO.service.FileInfoService;
import com.supermap.learning.minIO.util.SnowflakeIdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.Date;

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

    @Override
    public void save(String bucket, String objectName, @Nullable String md5, String fileName, String path) {
        FileInfoEntity fileInfoEntity = findByBucketAndObjectName(bucket, objectName);
        if (fileInfoEntity == null) {
            fileInfoEntity = new FileInfoEntity()
                    .setId(SnowflakeIdWorker.getInstance().nextId())
                    .setBucket(bucket)
                    .setObjectName(objectName)
                    .setPath(path)
                    .setFileName(fileName)
                    .setMd5(md5)
                    .setCreateTime(new Date())
                    .setUpdateTime(new Date());
            save(fileInfoEntity);
        } else {
            fileInfoEntity.setBucket(bucket)
                    .setObjectName(objectName)
                    .setPath(path.toString())
                    .setFileName(fileName)
                    .setMd5(md5)
                    .setCreateTime(new Date())
                    .setUpdateTime(new Date());
            save(fileInfoEntity);
        }
    }

}
