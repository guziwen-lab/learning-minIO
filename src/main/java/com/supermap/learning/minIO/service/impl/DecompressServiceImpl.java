package com.supermap.learning.minIO.service.impl;

import com.supermap.learning.minIO.common.constant.FileStateConstant;
import com.supermap.learning.minIO.config.BaseFileDirectoryConfigurationProperties;
import com.supermap.learning.minIO.service.DecompressService;
import com.supermap.learning.minIO.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author lty
 */
@Service
@Slf4j
public class DecompressServiceImpl implements DecompressService {

    @Autowired
    private BaseFileDirectoryConfigurationProperties baseFileDirectoryConfigurationProperties;

    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public void decompress(String target, String fileName) {
        CompletableFuture.runAsync(() -> FileUtil.unZip(
                        new File(baseFileDirectoryConfigurationProperties.getResource().concat(target)),
                        baseFileDirectoryConfigurationProperties.getUncompress().concat(fileName)),
                executor);
    }

    public Map<String, Integer> state(String fileName) {
        Map<String, Integer> resultData = new HashMap<>();
        String target = baseFileDirectoryConfigurationProperties.getUncompress().concat(fileName);
        File file = new File(target);
        if (!file.exists()) {
            resultData.put("state", FileStateConstant.ERROR);
            return resultData;
        }

        if (!file.isDirectory()) {
            throw new RuntimeException("解压后应该是一个目录");
        }

        File[] files = file.listFiles();
        if (files != null) {
            for (File child : files) {
                if (child.getName().equals("cgbunzip.info")) {
                    // 存在该文件说明正在解压，否则还没进行处理
                    Properties properties = new Properties();
                    try {
                        properties.load(new FileReader(child));
                        Object end = properties.get("end");
                        if (end == null) {
                            resultData.put("state", FileStateConstant.PROCESSING);
                        } else {
                            resultData.put("state", FileStateConstant.SUCCESS);
                        }
                        return resultData;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        resultData.put("state", FileStateConstant.PREPARING);
        return resultData;
    }

    @Override
    public Boolean deleteTemp(String fileName) {
        String target = baseFileDirectoryConfigurationProperties.getUncompress().concat(fileName);
        return FileUtil.deleteTempDir(target);
    }

}
