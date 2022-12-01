package com.supermap.learning.minIO.util;

import com.supermap.learning.minIO.bo.DecompressProgressBarBO;
import com.supermap.learning.minIO.common.constant.MinIOBucketConstant;
import com.supermap.learning.minIO.dto.MinIOComposeDTO;
import com.supermap.learning.minIO.entity.FileInfoEntity;
import com.supermap.learning.minIO.service.FileInfoService;
import io.minio.ObjectWriteArgs;
import io.minio.StatObjectResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
@Slf4j
public class MinioDecompressUtil {

    // 缓冲大小
    private static final int BUFFER_SIZE = 2048;
    // 压缩包分片解压上传的临界值 10M
    private static final long MAX_COMPRESS_FILE_THRESHOLD_COUNT = 10L * 1024 * 1024;

    @Autowired
    private MinIOTemplate minIOTemplate;

    @Autowired
    private FileInfoService fileInfoService;

    /**
     * 解压方法
     *
     * @param srcBucket        解压桶名称
     * @param srcObjectName    解压文件
     * @param targetObjectPath 对象路径
     */
    @SneakyThrows
    public Long decompress(String srcBucket, String srcObjectName, String targetObjectPath,
                           DecompressProgressBarBO decompressProgressBarBO) {
        StatObjectResponse stat = minIOTemplate.getStat(srcBucket, srcObjectName);
        // 判断源文件是否存在
        if (stat == null) {
            throw new RuntimeException("文件不存在，bucket：" + srcBucket + "，objectName：" + srcObjectName);
        }

        decompressProgressBarBO.setStartTime(new Date());
        decompressProgressBarBO.setTotal(stat.size());

        try (InputStream in = minIOTemplate.downloadObject(srcBucket, srcObjectName)) {
            try (ZipInputStream zipFile = new ZipInputStream(in)) {
                ZipEntry entry;
                while ((entry = zipFile.getNextEntry()) != null) {
                    if (entry.isDirectory() || entry.getName().contains("__MACOSX")) {
                        continue;
                    }

                    uploadToMinIO(targetObjectPath, zipFile, entry, stat.size());

                    decompressProgressBarBO.getDecompressedFiles().add(entry.getName());
                    decompressProgressBarBO.setRead(decompressProgressBarBO.getRead() + entry.getCompressedSize());
                }
            }

            // 解压完成
            decompressProgressBarBO.setRead(decompressProgressBarBO.getTotal());
            decompressProgressBarBO.setEndTime(new Date());

            FileInfoEntity fileInfoEntity = new FileInfoEntity()
                    .setId(SnowflakeIdWorker.getInstance().nextId())
                    .setBucket(MinIOBucketConstant.DECOMPRESS_BUCKET)
                    .setObjectName(targetObjectPath)
                    .setPath(targetObjectPath)
                    .setCreateTime(new Date())
                    .setUpdateTime(new Date());
            fileInfoService.save(fileInfoEntity);
            return fileInfoEntity.getId();
        }
    }

    /**
     * 上传到minIO
     *
     * @param targetObjectPath 对象路径
     * @param zipFile          ZipInputStream
     * @param entry            ZipEntry
     * @param totalSize        压缩包大小
     * @throws IOException IOException
     */
    private void uploadToMinIO(String targetObjectPath, ZipInputStream zipFile, ZipEntry entry, long totalSize)
            throws IOException {
        // 文件过大防止内存溢出，分片上传到minio
        if (totalSize > MAX_COMPRESS_FILE_THRESHOLD_COUNT) {
            List<MinIOComposeDTO> sources = new ArrayList<>();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buff = new byte[BUFFER_SIZE];
            int len;

            while ((len = zipFile.read(buff)) != -1) {
                bos.write(buff, 0, len);
                if (bos.size() > ObjectWriteArgs.MIN_MULTIPART_SIZE) {
                    bos = uploadMultipart(targetObjectPath, sources, bos);
                }
            }
            // 把剩余的流上传
            uploadMultipart(targetObjectPath, sources, bos);
            // 合成文件
            minIOTemplate.composeObject(sources, MinIOBucketConstant.DECOMPRESS_BUCKET,
                    targetObjectPath + entry.getName());
            // 删除分片
            minIOTemplate.removeDir(MinIOBucketConstant.CHUNK_BUCKET, targetObjectPath);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buff = new byte[BUFFER_SIZE];
            int len;
            while ((len = zipFile.read(buff)) != -1) {
                bos.write(buff, 0, len);
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            minIOTemplate.uploadObject(MinIOBucketConstant.DECOMPRESS_BUCKET,
                    targetObjectPath + entry.getName(), bis, bos.size());
        }
    }

    /**
     * 分片上传到minio时，先上传一部分
     *
     * @param targetObjectPath 对象路径
     * @param sources          分片源文件
     * @param bos              bos里面包含要上传的字节流
     * @return 返回一个新的ByteArrayOutputStream
     */
    @NotNull
    private ByteArrayOutputStream uploadMultipart(String targetObjectPath, List<MinIOComposeDTO> sources,
                                                  ByteArrayOutputStream bos) {
        String chunkName = targetObjectPath + UUIDUtil.get();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        minIOTemplate.uploadObject(MinIOBucketConstant.CHUNK_BUCKET, chunkName, bis, bos.size());

        MinIOComposeDTO minIOComposeDTO = new MinIOComposeDTO()
                .setBucket(MinIOBucketConstant.CHUNK_BUCKET)
                .setObject(chunkName);
        sources.add(minIOComposeDTO);

        bos = new ByteArrayOutputStream();
        return bos;
    }

}
