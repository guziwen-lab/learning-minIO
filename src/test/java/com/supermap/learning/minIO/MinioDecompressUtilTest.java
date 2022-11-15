package com.supermap.learning.minIO;

import com.supermap.learning.minIO.common.constant.MinIOBucketConstant;
import com.supermap.learning.minIO.config.MinIOConfigurationProperties;
import com.supermap.learning.minIO.util.MinIOTemplate;
import com.supermap.learning.minIO.util.MinioDecompressUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;

/**
 * @author lty
 */
@SpringBootTest
@Slf4j
public class MinioDecompressUtilTest {

    @Autowired
    private MinIOConfigurationProperties minIOConfigurationProperties;

    @Autowired
    private MinIOTemplate minIOTemplate;

    @Autowired
    private MinioDecompressUtil minioDecompressUtil;

    @Test
    public void unCompressTest() throws IOException {
        String zipPath = "/Users/guziwen/Java/temp/resource/压缩包.zip";
        File file = new File(zipPath);
        minioDecompressUtil.decompress(zipPath, MinIOBucketConstant.DECOMPRESS_BUCKET, "test/",null);
    }

}
