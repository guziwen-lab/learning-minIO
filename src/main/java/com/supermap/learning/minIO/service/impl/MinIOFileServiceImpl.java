package com.supermap.learning.minIO.service.impl;

import com.alibaba.fastjson.JSON;
import com.supermap.learning.minIO.bo.DecompressProgressBarBO;
import com.supermap.learning.minIO.bo.FileStateBO;
import com.supermap.learning.minIO.common.constant.SpecialFileConstant;
import com.supermap.learning.minIO.config.MinIOConfigurationProperties;
import com.supermap.learning.minIO.dto.MinIOComposeDTO;
import com.supermap.learning.minIO.dto.UploadUrlDTO;
import com.supermap.learning.minIO.entity.FileInfoEntity;
import com.supermap.learning.minIO.service.FileInfoService;
import com.supermap.learning.minIO.service.MinIOFileService;
import com.supermap.learning.minIO.util.MinIOTemplate;
import com.supermap.learning.minIO.util.MinioDecompressUtil;
import com.supermap.learning.minIO.util.SnowflakeIdWorker;
import com.supermap.learning.minIO.util.UUIDUtil;
import com.supermap.learning.minIO.vo.FileStateVO;
import com.supermap.learning.minIO.vo.UploadUrlVO;
import com.supermap.learning.minIO.common.constant.MinIOBucketConstant;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author lty
 */
@Service
@Slf4j
public class MinIOFileServiceImpl implements MinIOFileService {

    @Autowired
    private MinIOTemplate minIOTemplate;

    @Autowired
    private MinIOConfigurationProperties minIOConfigurationProperties;

    @Autowired
    private FileInfoService fileInfoService;

    @Autowired
    private MinioDecompressUtil minioDecompressUtil;

    @Autowired
    private ThreadPoolExecutor executor;

    private static final Map<Long, DecompressProgressBarBO> minioStateList = new HashMap<>();

    @Override
    public Boolean upload(MultipartFile file) throws IOException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDateFormat.format(new Date());

        String path = date.concat("/").concat(UUID.randomUUID().toString()).concat("/");
        String fileName = path.concat(Objects.requireNonNull(file.getOriginalFilename()));

        FileInfoEntity fileInfoEntity = new FileInfoEntity();
        fileInfoEntity.setId(SnowflakeIdWorker.getInstance().nextId());
        fileInfoEntity.setBucket(MinIOBucketConstant.TEST);
        fileInfoEntity.setObjectName(fileName);
        fileInfoEntity.setPath(path);
        fileInfoEntity.setFileName(file.getOriginalFilename());
        fileInfoService.save(fileInfoEntity);

        minIOTemplate.uploadObject(MinIOBucketConstant.TEST, fileName, file.getInputStream());
        return true;
    }

    @Override
    public Boolean upload(String bucket, String objectName, String md5, MultipartFile file) throws IOException {
        String[] split = objectName.split("/");
        String fileName = split[split.length - 1];
        StringBuilder path = new StringBuilder();
        for (int i = 0; i < split.length - 1; i++) {
            path.append(split[i]);
        }
        path.append("/");

        FileInfoEntity fileInfoEntity = new FileInfoEntity()
                .setId(SnowflakeIdWorker.getInstance().nextId())
                .setBucket(bucket)
                .setObjectName(objectName)
                .setPath(path.toString())
                .setFileName(fileName)
                .setMd5(md5)
                .setCreateTime(new Date())
                .setUpdateTime(new Date());
        fileInfoService.save(fileInfoEntity);

        minIOTemplate.uploadObject(bucket, objectName, file.getInputStream());
        return true;
    }

    @Override
    public void download(String bucket, String fileName, HttpServletResponse response) throws IOException {
        FileInfoEntity fileInfoEntity = fileInfoService.findByBucketAndObjectName(bucket, fileName);
        if (fileInfoEntity == null) {
            throw new RuntimeException("该文件不存在");
        }

        InputStream in = minIOTemplate.downloadObject(bucket, fileName);
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(fileInfoEntity.getFileName(), StandardCharsets.UTF_8));
        ServletOutputStream out = response.getOutputStream();

        int len;
        byte[] buffer = new byte[2048];
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();
    }

    @Override
    public String getUrl(String bucket, String objectName) {
        return minIOTemplate.getObjectUrl(bucket, objectName);
    }

    @Override
    public void composeObject(List<MinIOComposeDTO> minIOComposeDTO, String destBucketName, String destObjectName) {
        minIOTemplate.composeObject(minIOComposeDTO, destBucketName, destObjectName);
    }

    @Override
    public FileStateVO isUploaded(String md5) {
        FileStateVO vo = new FileStateVO();

        StatObjectResponse stat = minIOTemplate.getStat(MinIOBucketConstant.CHUNK_BUCKET,
                MinIOTemplate.getMD5ObjectName(md5));
        if (stat == null) {
            vo.setIsUploaded(false);
            return vo;
        }

        List<Item> items = minIOTemplate.listObjects(MinIOBucketConstant.CHUNK_BUCKET, md5.concat("/"));
        // 获取分片文件
        List<String> chunkIndex = items.stream().map(item -> {
            String s = item.objectName();
            String[] split = s.split("/");
            // 获取文件名
            String fileName = split[split.length - 1];
            // 跳过信息文件
            if (SpecialFileConstant.FILE_STATE_FILE_NAME.equals(fileName)) {
                return null;
            }
            return fileName;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        vo.setChunkIndex(chunkIndex);
        vo.setIsUploaded(true);
        return vo;
    }

    @Override
    public UploadUrlVO getPresignedPostFormData(String bucket, String objectName) {
        UploadUrlVO uploadUrlVO = new UploadUrlVO();

        Map<String, String> presignedPostFormData = minIOTemplate.getPresignedPostFormData(bucket, objectName);
        uploadUrlVO.setFormData(presignedPostFormData);

        String endpoint = minIOConfigurationProperties.getEndpoint();
        String url = endpoint.concat("/").concat(bucket);
        uploadUrlVO.setUrl(url);

        return uploadUrlVO;
    }

    @Override
    public List<UploadUrlVO> getPresignedPostFormData(UploadUrlDTO uploadUrlDTO) {
        String path = uploadUrlDTO.getMd5() + "/";
        String destinationPath = UUIDUtil.get() + "/";

        // 创建目录和信息文件
        FileStateBO fileStateBO = new FileStateBO()
                .setBucket(MinIOBucketConstant.COMPOSE_BUCKET)
                .setPath(destinationPath)
                .setObjectName(destinationPath + uploadUrlDTO.getFileName())
                .setFileName(uploadUrlDTO.getFileName())
                .setMd5(uploadUrlDTO.getMd5())
                .setChunkNum(uploadUrlDTO.getChunkNum());
        minIOTemplate.uploadObject(MinIOBucketConstant.CHUNK_BUCKET,
                MinIOTemplate.getMD5ObjectName(uploadUrlDTO.getMd5()),
                new ByteArrayInputStream(JSON.toJSONBytes(fileStateBO)));

        // 获取URL和签名
        List<UploadUrlVO> vos = new ArrayList<>();
        for (int i = 0; i < uploadUrlDTO.getChunkNum(); i++) {
            UploadUrlVO uploadUrlVO = new UploadUrlVO();
            uploadUrlVO.setIndex(i);

            Map<String, String> presignedPostFormData = minIOTemplate.getPresignedPostFormData(
                    MinIOBucketConstant.CHUNK_BUCKET, path + i);
            presignedPostFormData.put("Content-Type", "*/*");
            presignedPostFormData.put("key", path + i);
            uploadUrlVO.setFormData(presignedPostFormData);

            String url = minIOConfigurationProperties.getEndpoint().concat("/")
                    .concat(MinIOBucketConstant.CHUNK_BUCKET);
            uploadUrlVO.setUrl(url);

            vos.add(uploadUrlVO);
        }

        return vos;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void composeObject(String md5) throws IOException {
        StatObjectResponse stat = minIOTemplate.getStat(MinIOBucketConstant.CHUNK_BUCKET,
                MinIOTemplate.getMD5ObjectName(md5));
        if (stat == null) {
            throw new RuntimeException("分片文件还未上传");
        }

        // 读取信息文件
        InputStream in = minIOTemplate.downloadObject(MinIOBucketConstant.CHUNK_BUCKET,
                MinIOTemplate.getMD5ObjectName(md5));
        FileStateBO fileStateBO = JSON.parseObject(in, FileStateBO.class);
        Integer chunkNum = fileStateBO.getChunkNum();

        String path = md5 + "/";
        List<MinIOComposeDTO> sources = new ArrayList<>();
        for (int i = 0; i < chunkNum; i++) {
            MinIOComposeDTO minIOComposeDTO = new MinIOComposeDTO();
            minIOComposeDTO.setBucket(MinIOBucketConstant.CHUNK_BUCKET);
            minIOComposeDTO.setObject(path + i);
            sources.add(minIOComposeDTO);
        }

        // 合成文件
        minIOTemplate.composeObject(sources, MinIOBucketConstant.COMPOSE_BUCKET, fileStateBO.getObjectName());
        FileInfoEntity fileInfoEntity = new FileInfoEntity()
                .setId(SnowflakeIdWorker.getInstance().nextId())
                .setBucket(MinIOBucketConstant.COMPOSE_BUCKET)
                .setObjectName(fileStateBO.getObjectName())
                .setPath(fileStateBO.getPath())
                .setFileName(fileStateBO.getFileName())
                .setMd5(md5)
                .setCreateTime(new Date())
                .setUpdateTime(new Date());
        fileInfoService.save(fileInfoEntity);

        // 删除分片文件
        minIOTemplate.removeDir(MinIOBucketConstant.CHUNK_BUCKET, path);
    }

    @Override
    public String getUrl(Long fileId) {
        FileInfoEntity fileInfoEntity = fileInfoService.getOne(fileId);
        String bucket = fileInfoEntity.getBucket();
        String objectName = fileInfoEntity.getObjectName();

        return minIOTemplate.getObjectUrl(bucket, objectName);
    }

    @Override
    public Long decompress(Long compressFileId) throws ExecutionException, InterruptedException {
        DecompressProgressBarBO decompressProgressBarBO = new DecompressProgressBarBO();
        minioStateList.put(compressFileId, decompressProgressBarBO);

        FileInfoEntity fileInfoEntity = fileInfoService.getOne(compressFileId);
        String bucket = fileInfoEntity.getBucket();
        String objectName = fileInfoEntity.getObjectName();

        CompletableFuture<Long> longCompletableFuture = CompletableFuture.supplyAsync(() ->
                minioDecompressUtil.decompress(bucket, objectName, UUIDUtil.get() + "/",
                        decompressProgressBarBO), executor);
        return longCompletableFuture.get();
    }

    @Override
    public DecompressProgressBarBO decompressState(Long compressFileId) {
        return minioStateList.get(compressFileId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void decompressDelete(Long fileId) {
        FileInfoEntity fileInfoEntity = fileInfoService.getOne(fileId);
        fileInfoService.deleteById(fileId);
        minIOTemplate.removeDir(fileInfoEntity.getBucket(), fileInfoEntity.getPath());
    }

}
