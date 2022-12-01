package com.supermap.learning.minIO.service;

import com.supermap.learning.minIO.bo.DecompressProgressBarBO;
import com.supermap.learning.minIO.dto.MinIOComposeDTO;
import com.supermap.learning.minIO.dto.UploadUrlDTO;
import com.supermap.learning.minIO.vo.FileStateVO;
import com.supermap.learning.minIO.vo.UploadUrlVO;
import io.minio.StatObjectResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author lty
 */
public interface MinIOFileService {

    StatObjectResponse getStat(String bucketName, String objectName);

    Boolean upload(MultipartFile uploadFile) throws IOException;

    Boolean upload(String bucket, String objectName, String md5, MultipartFile file) throws IOException;

    void download(String bucket, String fileName, HttpServletResponse response) throws IOException;

    String getUrl(String bucket, String objectName);

    void composeObject(List<MinIOComposeDTO> minIOComposeDTO, String destBucketName, String destObjectName);


    FileStateVO isUploaded(String md5);

    UploadUrlVO getPresignedPostFormData(String bucket, String objectName);

    List<UploadUrlVO> getPresignedPostFormData(UploadUrlDTO uploadUrlDTO);

    void composeObject(String md5) throws IOException;

    String getUrl(Long fileId);

    Long decompress(Long compressFileId) throws ExecutionException, InterruptedException;

    DecompressProgressBarBO decompressState(Long compressFileId);

    void decompressDelete(Long fileId);

}
