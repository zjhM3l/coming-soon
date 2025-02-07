package com.sky.controller.admin;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * 通用接口
 */ 
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传
     * @param file
     * @return
     */
    // 返回的是文件的访问路径，泛型为String
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {
        // 初始AliOssUtil对象，通过注解自动注入
        // AliOssUtill的完整初始化流程：
        // 1. 在AliOssProperties.java提供配置属性类（配置属性类，用于读取配置文件中对应的的配置项然后封装成java对象）
        // 2. 在yml文件中配置相关属性
        // 3. 在OssConfiguration.java中创建AliOssUtil对象（@ConditionalOnMissingBean全局唯一即可）
        // 4. 在CommonController.java中使用@Autowired注解自动注入AliOssUtil对象
        // 这样的话就可以直接使用AliOssUtil对象和相关方法了

        try {
            // 原始文件名
            String originalFilename = file.getOriginalFilename();
            // 截取文件后缀名
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            // uuid构造文件名
            String objectName = UUID.randomUUID().toString() + extension;
            // 文件请求路径
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(filePath);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败");
        }
    }
}
