package com.supermap.learning.minIO.controller;

import com.supermap.learning.minIO.bo.DecompressProgressBarBO;
import com.supermap.learning.minIO.dto.ComposeDTO;
import com.supermap.learning.minIO.dto.UploadUrlDTO;
import com.supermap.learning.minIO.service.MinIOFileService;
import com.supermap.learning.minIO.vo.FileStateVO;
import com.supermap.learning.minIO.vo.UploadUrlVO;
import com.supermap.learning.minIO.common.Response.R;
import io.minio.StatObjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author lty
 */
@RestController
@RequestMapping("minio")
@Slf4j
public class MinIOFileController {

    @Autowired
    private MinIOFileService minIOFileService;

    /**
     * 上传文件，路径不可变
     *
     * @param file file
     */
    @PostMapping("upload/file")
    public R<Boolean> upload(MultipartFile file) throws IOException {
        Boolean upload = minIOFileService.upload(file);
        return R.ok(upload);
    }

    /**
     * 上传到给定的桶、对象名称
     *
     * @param bucket     桶名称
     * @param objectName 对象名称
     * @param md5        md5
     * @param file       file
     */
    @PostMapping("upload/file/bucket/objectName/md5")
    public R<Boolean> upload(@RequestParam String bucket, @RequestParam String objectName,
                             @RequestParam(required = false) String md5, MultipartFile file) throws IOException {
        long startTime = System.currentTimeMillis();
        Boolean upload = minIOFileService.upload(bucket, objectName, md5, file);
        long endTime = System.currentTimeMillis();
        double fileSize = file.getSize() * 1d / 1024 / 1024;
        double time = (endTime - startTime) / 1000d;
        double rate = fileSize / time;
        log.info("文件大小:{}MB, 上传时间: {}s, 速率: {}MB/s", fileSize, time, rate);
        return R.ok(upload);
    }

    /**
     * 直接下载文件
     *
     * @param bucket   桶名称
     * @param fileName 对象名称
     */
    @GetMapping("download/file")
    public R<Void> download(@RequestParam String bucket, @RequestParam String fileName,
                            HttpServletResponse response) throws IOException {
        long startTime = System.currentTimeMillis();
        minIOFileService.download(bucket, fileName, response);
        long endTime = System.currentTimeMillis();

        StatObjectResponse stat = minIOFileService.getStat(bucket, fileName);
        double fileSize = stat.size() * 1d / 1024 / 1024;
        double time = (endTime - startTime) / 1000d;
        double rate = fileSize / time;
        log.info("文件大小:{}MB, 下载时间: {}s, 速率: {}MB/s", fileSize, time, rate);

        return R.ok();
    }

    /**
     * 获取下载连接
     *
     * @param bucket     桶名称
     * @param objectName 对象名称
     */
    @GetMapping("download/url")
    public R<String> getDownloadUrl(@RequestParam String bucket, @RequestParam String objectName) {
        String url = minIOFileService.getUrl(bucket, objectName);
        return R.ok(url);
    }

    /**
     * 获取上传连接
     *
     * @param bucket     桶名称
     * @param objectName 对象名称
     * @return UploadUrlVO
     */
    @GetMapping("upload/url")
    public R<UploadUrlVO> getUploadUrl(@RequestParam String bucket, @RequestParam String objectName) {
        return R.ok(minIOFileService.getPresignedPostFormData(bucket, objectName));
    }

    /**
     * 将分片合成为一个完整的大文件
     *
     * @param composeDTO ComposeDTO
     */
    @PostMapping("compose")
    public R<Void> composeObject(@RequestBody ComposeDTO composeDTO) {
        minIOFileService.composeObject(composeDTO.getMinIOComposeDTO(),
                composeDTO.getDestBucketName(),
                composeDTO.getDestObjectName());
        return R.ok();
    }

    /*-----------------------------------------以上为测试开发用--------------------------------------------*/

    /**
     * 判断文件是否上传过
     *
     * @param md5 md5
     * @return FileStateVO
     */
    @GetMapping("uploaded")
    public R<FileStateVO> isUploaded(String md5) {
        FileStateVO vo = minIOFileService.isUploaded(md5);
        return R.ok(vo);
    }

    /**
     * 批量获取上传连接
     *
     * @param uploadUrlDTO uploadUrlDTO
     * @return UploadUrlVO
     */
    @PostMapping("batch/upload/url")
    public R<List<UploadUrlVO>> getUploadUrl(@RequestBody UploadUrlDTO uploadUrlDTO) {
        return R.ok(minIOFileService.getPresignedPostFormData(uploadUrlDTO));
    }

    /**
     * 合成文件
     *
     * @param md5 md5
     */
    @GetMapping("compose/{md5}")
    public R<Void> compose(@PathVariable String md5) throws IOException {
        minIOFileService.composeObject(md5);
        return R.ok();
    }

    /**
     * 获取下载连接
     *
     * @param fileId 文件id
     */
    @GetMapping("download/{fileId}")
    public R<String> getDownloadUrl(@PathVariable Long fileId) {
        String url = minIOFileService.getUrl(fileId);
        return R.ok(url);
    }

    /**
     * 解压文件
     *
     * @param compressFileId 压缩文件id
     */
    @GetMapping("decompress")
    public R<Long> decompress(@RequestParam Long compressFileId) throws ExecutionException, InterruptedException {
        long startTime = System.currentTimeMillis();

        Long fileId = minIOFileService.decompress(compressFileId);

        long endTime = System.currentTimeMillis();
        double time = (endTime - startTime) / 1000d;
        log.info("解压时间: {}s", time);

        return R.ok(fileId);
    }

    /**
     * 获取解压进度
     *
     * @param compressFileId 压缩文件id
     */
    @GetMapping("decompress/state")
    public R<DecompressProgressBarBO> decompressState(@RequestParam Long compressFileId) {
        DecompressProgressBarBO decompressProgressBarBO = minIOFileService.decompressState(compressFileId);
        return R.ok(decompressProgressBarBO);
    }

    /**
     * 删除解压后的文件夹
     *
     * @param fileId fileId
     */
    @DeleteMapping("decompress/delete")
    public R<Void> decompressDelete(@RequestParam Long fileId) {
        minIOFileService.decompressDelete(fileId);
        return R.ok();
    }

}
