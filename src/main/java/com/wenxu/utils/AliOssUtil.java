package com.wenxu.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayInputStream;
import java.util.UUID;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传
     * @param bytes 文件字节数组
     * @param objectName 原始文件名
     * @return 返回上传成功后的图片 URL 网址
     */
    public String upload(byte[] bytes, String objectName) {
        // 生成唯一文件名，防止覆盖 (UUID + 原始后缀)
        String extension = objectName.substring(objectName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + extension;

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            ossClient.putObject(bucketName, fileName, new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        // 拼接成可以访问的 URL
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(bucketName)
                .append(".")
                .append(endpoint)
                .append("/")
                .append(fileName);

        log.info("文件上传成功，访问路径为: {}", stringBuilder.toString());
        return stringBuilder.toString();
    }
}