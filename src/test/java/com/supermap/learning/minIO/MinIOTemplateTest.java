package com.supermap.learning.minIO;

import com.alibaba.fastjson.JSON;
import com.supermap.learning.minIO.bo.FileStateBO;
import com.supermap.learning.minIO.common.constant.MinIOBucketConstant;
import com.supermap.learning.minIO.common.constant.SpecialFileConstant;
import com.supermap.learning.minIO.config.MinIOConfigurationProperties;
import com.supermap.learning.minIO.util.MinIOTemplate;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author lty
 */
@SpringBootTest
@Slf4j
public class MinIOTemplateTest {

    @Autowired
    private MinIOConfigurationProperties minIOConfigurationProperties;

    @Autowired
    private MinIOTemplate minIOTemplate;

    @Test
    void getStatTest() {
        StatObjectResponse stat = minIOTemplate.getStat(MinIOBucketConstant.CHUNK_BUCKET, "4/fileState.json");
        log.info("{}", stat);
    }

    @Test
    void listObjectsTest() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minIOConfigurationProperties.getEndpoint())
                .credentials(minIOConfigurationProperties.getAccessKey(), minIOConfigurationProperties.getSecretKey())
                .build();

        String bucketName = MinIOBucketConstant.CHUNK_BUCKET;
        String objectName = "0372ea44e1d94a4faacb3a967fa4097e/";

        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(objectName)
                .recursive(true)
                .build());

        for (Result<Item> result : results) {
            log.info("{}", result.get().objectName());
        }
    }

    @Test
    void uploadObjectTest() {
        String md5 = "1";
        Integer chunkNum = 10;

        FileStateBO fileStateBO = new FileStateBO()
                .setBucket(MinIOBucketConstant.CHUNK_BUCKET)
                .setPath(md5 + "/")
                .setObjectName(MinIOTemplate.getMD5ObjectName(md5))
                .setFileName(SpecialFileConstant.FILE_STATE_FILE_NAME)
                .setMd5(md5)
                .setChunkNum(chunkNum);

        minIOTemplate.uploadObject(MinIOBucketConstant.CHUNK_BUCKET,
                fileStateBO.getObjectName(), new ByteArrayInputStream(JSON.toJSONBytes(fileStateBO)));
    }

    @Test
    void removeObjectTest() {
        minIOTemplate.removeDir(MinIOBucketConstant.CHUNK_BUCKET, "2/");
    }

}
