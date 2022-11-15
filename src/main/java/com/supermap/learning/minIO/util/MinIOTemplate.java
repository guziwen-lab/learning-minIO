package com.supermap.learning.minIO.util;

import com.supermap.learning.minIO.common.constant.SpecialFileConstant;
import com.supermap.learning.minIO.config.MinIOConfigurationProperties;
import com.supermap.learning.minIO.dto.MinIOComposeDTO;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@Configuration
@EnableConfigurationProperties({MinIOConfigurationProperties.class})
public class MinIOTemplate {

    private final MinioClient minioClient;

    private final MinIOConfigurationProperties minIOConfigurationProperties;

    public MinIOTemplate(MinIOConfigurationProperties minIOConfigurationProperties) {
        this.minIOConfigurationProperties = minIOConfigurationProperties;
        minioClient = MinioClient.builder()
                .endpoint(minIOConfigurationProperties.getEndpoint())
                .credentials(minIOConfigurationProperties.getAccessKey(), minIOConfigurationProperties.getSecretKey())
                .build();
    }

    /**
     * 查询所有存储桶
     *
     * @return Bucket 集合
     */
    @SneakyThrows
    public List<Bucket> listBuckets() {
        return minioClient.listBuckets();
    }

    /**
     * 判断 bucket是否存在
     */
    @SneakyThrows
    public boolean bucketExists(String bucketName) {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    /**
     * 创建 bucket
     */
    @SneakyThrows
    public void makeBucket(String bucketName) {
        boolean isExist = bucketExists(bucketName);
        if (!isExist) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    /**
     * 文件上传
     *
     * @param bucketName bucket名称
     * @param objectName 对象名称，文件名称
     * @param filepath   文件路径
     */
    @SneakyThrows
    public ObjectWriteResponse uploadObject(String bucketName, String objectName, String filepath) {
        makeBucket(bucketName);
        return minioClient.uploadObject(UploadObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .filename(filepath).build());
    }

    /**
     * 文件上传
     *
     * @param bucketName  bucket名称
     * @param objectName  对象名称，文件名称
     * @param inputStream 文件输入流
     */
    @SneakyThrows
    public ObjectWriteResponse uploadObject(String bucketName, String objectName, InputStream inputStream) {
        makeBucket(bucketName);
        return minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(inputStream, inputStream.available(), -1)
                .build());
    }

    /**
     * 删除文件
     *
     * @param bucketName bucket名称
     * @param objectName 对象名称
     */
    @SneakyThrows
    public void removeObject(String bucketName, String objectName) {
        if (bucketExists(bucketName)) {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        }
    }

    /**
     * 删除目录
     *
     * @param bucketName bucket名称
     * @param dir        目录
     */
    @SneakyThrows
    public void removeDir(String bucketName, String dir) {
        if (bucketExists(bucketName)) {
            Iterable<Result<Item>> objects = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(dir)
                    .recursive(true)
                    .build());

            List<DeleteObject> deleteObjects = new LinkedList<>();
            for (Result<Item> result : objects) {
                deleteObjects.add(new DeleteObject(result.get().objectName()));
            }
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(deleteObjects).build());
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                log.error("Error in deleting object {}; " + error.message());
            }
        }
    }

    /**
     * 下载文件到目的路径
     *
     * @param bucketName  bucket名称
     * @param objectName  对象名称
     * @param destination 目的地
     */
    @SneakyThrows
    public void downloadObject(String bucketName, String objectName, String destination) {
        minioClient.downloadObject(DownloadObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .filename(destination)
                .build());
    }

    /**
     * 下载文件
     *
     * @param bucketName bucket名称
     * @param objectName 对象名称
     * @return 实际是一个InputStream
     */
    @SneakyThrows
    public GetObjectResponse downloadObject(String bucketName, String objectName) {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }

    /**
     * 获取文件url
     *
     * @param bucketName bucket名称
     * @param objectName 对象名称
     * @return url
     */
    @SneakyThrows
    public String getObjectUrl(String bucketName, String objectName) {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .method(Method.GET)
                .expiry(minIOConfigurationProperties.getPresignedObjectUrlExpiration(), TimeUnit.DAYS)
                .build());
    }

    /**
     * 获取带签名的临时上传元数据对象，前端可获取后，直接上传到Minio
     *
     * @param bucketName bucket名称
     * @param objectName 对象名称
     * @return formData
     */
    @SneakyThrows
    public Map<String, String> getPresignedPostFormData(String bucketName, String objectName) {
        // 为存储桶创建一个上传策略，过期时间为7天
        PostPolicy policy = new PostPolicy(bucketName,
                ZonedDateTime.now().plusDays(minIOConfigurationProperties.getPresignedPostFormDataExpiration()));
        // 设置一个参数key，值为上传对象的名称
        policy.addEqualsCondition("key", objectName);
        // 添加Content-Type，例如以"image/"开头，表示只能上传照片，这里支持所有
        policy.addStartsWithCondition("Content-Type", MediaType.ALL_VALUE);
        // 设置上传文件的大小 64kiB to 10MiB.
        //policy.addContentLengthRangeCondition(64 * 1024, 10 * 1024 * 1024);

        return minioClient.getPresignedPostFormData(policy);
    }

    /**
     * 合成对象
     *
     * @param sources        分片文件
     * @param destBucketName 目的桶名称
     * @param destObjectName 目的对象名称
     */
    @SneakyThrows
    public void composeObject(List<MinIOComposeDTO> sources, String destBucketName, String destObjectName) {
        makeBucket(destBucketName);

        List<ComposeSource> sourceObjectList = sources.stream().map(dto -> {
            String bucket = dto.getBucket();
            String object = dto.getObject();
            return ComposeSource.builder().bucket(bucket).object(object).build();
        }).collect(Collectors.toList());

        minioClient.composeObject(ComposeObjectArgs.builder()
                .bucket(destBucketName)
                .object(destObjectName)
                .sources(sourceObjectList)
                .build());
    }

    /**
     * Gets object information and metadata of an object.
     *
     * @param bucketName bucket名称
     * @param objectName 对象名称
     * @return StatObjectResponse
     */
    public StatObjectResponse getStat(String bucketName, String objectName) {
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            log.error("获取object信息错误", e);
            return null;
        }
    }

    /**
     * Lists object information of a bucket.
     *
     * @param bucketName bucket名称
     */
    @SneakyThrows
    public List<Item> listObjects(String bucketName, String path) {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(path)
                .recursive(false)
                .build());
        List<Item> items = new ArrayList<>();
        for (Result<Item> result : results) {
            items.add(result.get());
        }
        return items;
    }

    /**
     * 获取md5目录下的信息文件的对象名称
     *
     * @param md5 md5
     * @return 对象名称
     */
    public static String getMD5ObjectName(String md5) {
        return md5 + "/" + SpecialFileConstant.FILE_STATE_FILE_NAME;
    }

}