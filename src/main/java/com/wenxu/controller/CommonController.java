package com.wenxu.controller;

import com.wenxu.common.ApiMessages;
import com.wenxu.common.Result;
import com.wenxu.utils.AliOssUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/common")
@Slf4j
public class CommonController {

    @Resource
    private AliOssUtil aliOssUtil;

    /**
     * 通用文件上传接口
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(ApiMessages.UPLOAD_FILE_EMPTY);
        }
        if (!StringUtils.hasText(file.getOriginalFilename())) {
            return Result.error(ApiMessages.UPLOAD_FILENAME_EMPTY);
        }

        log.info("文件上传：{}", file.getOriginalFilename());
        try {
            String filePath = aliOssUtil.upload(file.getBytes(), file.getOriginalFilename());
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error(ApiMessages.UPLOAD_FAILED);
        }
    }
}
