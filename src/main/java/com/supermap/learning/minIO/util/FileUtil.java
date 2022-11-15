package com.supermap.learning.minIO.util;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
public class FileUtil {

    private final static int BUFFER_SIZE = 1024;

    public static final String DATE_PATTERN = "yyyy-MM-dd HH/mm/ss";

    // 切分文件
    public static void main(String[] args) throws IOException {
        //要分割出来的文件的大小
        int size = 1024 * 1024 * 20; //10M

        BufferedInputStream in = new BufferedInputStream(Files.newInputStream(new File("/Users/guziwen/Java/temp/resource/temp.zip").toPath()));
        int available = in.available();
        int num = (int) Math.ceil((double) available / size);

        int len;
        for (int i = 0; i < num; i++) { //8.85M的文件分割成8个1M的，和一个0.85M的
            File file = new File("/Users/guziwen/Java/temp/resource/chunk/" + i + "temp.temp");//分割的文件格式可以随便设置，只要文件合并时名称一致即可
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            BufferedOutputStream out = new BufferedOutputStream(outputStream);

            int count = 0;
            byte[] buf = new byte[1024 * 1024 * 10];//每次读取10M,数组大小不能太大，会内存溢出，通过目标文件大小size判断一下
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
                count += len;
                if (count >= size) {
                    break;//每次读取1M，然后写入到文件中
                }
            }

            out.flush();
            out.close();
            outputStream.close();

            System.out.println("文件已完成：" + file.getName());
        }

        System.out.println("文件已完成分割====");
    }

    /**
     * zip解压
     *
     * @param srcFile     zip源文件
     * @param destDirPath 解压后的目标文件夹
     * @throws RuntimeException 解压失败会抛出运行时异常
     */
    public static void unZip(File srcFile, String destDirPath) throws RuntimeException {
        Date start = new Date();

        // 判断源文件是否存在
        if (!srcFile.exists()) {
            log.error("所指文件不存在{}", srcFile.getPath());
        }

        // 先创建目的目录
        File file = new File(destDirPath);
        if (!file.exists()) {
            boolean mkdirsState = file.mkdirs();
            log.info("目的目录是否创建成功{}", mkdirsState);
        }
        // 写入info文件
        File infoFile = new File(destDirPath + "/cgbunzip.info");
        try {
            boolean newFileState = infoFile.createNewFile();
            log.info("info文件是否创建成功：{}", newFileState);
            Properties properties = new Properties();
            properties.load(new FileReader(infoFile));
            properties.put("start", DateUtil.format(start, DATE_PATTERN));
            properties.put("deletable", "true");
            properties.store(new FileWriter(infoFile), "info");
        } catch (IOException e) {
            log.error("", e);
        }

        // 开始解压
        log.info("线程{} 开始解压", Thread.currentThread().getName());

        // 去掉最后一级目录
        String[] split = destDirPath.split("/");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < split.length - 1; i++) {
            stringBuilder.append(split[i]).append("/");
        }
        destDirPath = stringBuilder.toString();

        try (ZipFile zipFile = new ZipFile(srcFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            int i = 0;

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (i++ == 0 || entry.getName().contains("__MACOSX")) {
                    continue;
                }
                // 如果是文件夹，就创建个文件夹
                if (entry.isDirectory()) {
                    String dirPath = destDirPath + "/" + entry.getName();
                    File dir = new File(dirPath);
                    dir.mkdirs();
                } else {
                    // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                    File targetFile = new File(destDirPath + "/" + entry.getName());
                    // 保证这个文件的父文件夹必需存在
                    if (!targetFile.getParentFile().exists()) {
                        targetFile.getParentFile().mkdirs();
                    }
                    targetFile.createNewFile();

                    // 将压缩文件内容写入到这个文件中
                    InputStream is = zipFile.getInputStream(entry);
                    FileOutputStream fos = new FileOutputStream(targetFile);
                    int len;
                    byte[] buf = new byte[BUFFER_SIZE];
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }

                    targetFile.setReadable(true, false);

                    // 关流顺序，先打开的后关闭
                    fos.close();
                    is.close();
                }
            }

            Date end = new Date();
            try {
                Properties properties = new Properties();
                properties.load(new FileReader(infoFile));
                properties.put("start", DateUtil.format(start, DATE_PATTERN));
                properties.put("end", DateUtil.format(end, DATE_PATTERN));
                properties.put("deletable", "true");
                properties.store(new FileWriter(infoFile), "info");
            } catch (IOException e) {
                log.error("", e);
            }
            log.info("线程{} 解压完成，耗时{}s", Thread.currentThread().getName(), (end.getTime() - start.getTime()) / 1000d);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public static boolean deleteTempDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return true;
        }

        if (!file.isDirectory()) {
            throw new RuntimeException("只支持删除解压后的目录");
        }

        File[] files = file.listFiles();
        if (files != null) {
            for (File child : files) {
                if (child.getName().equals("cgbunzip.info")) {
                    Properties properties = new Properties();
                    try {
                        properties.load(new FileReader(child));
                        String deletable = (String) properties.get("deletable");
                        if ("true".equals(deletable)) {
                            deleteDir(file);
                            return true;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return false;
    }

    public static void deleteDir(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDir(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    /**
     * 获取文件名后缀
     *
     * @param fileName 文件名
     * @return 后缀字符串
     */
    public static String getPostfix(String fileName) {
        String[] s = fileName.split("\\.");
        String postfix = fileName;
        if (s.length > 1) {
            postfix = s[s.length - 1];
        }
        return postfix;
    }

}
