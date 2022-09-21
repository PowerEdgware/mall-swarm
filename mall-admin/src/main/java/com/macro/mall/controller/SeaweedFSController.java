package com.macro.mall.controller;

import cn.hutool.core.collection.CollUtil;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.common.oss.client.AmazonS3ClientHelper;
import com.macro.mall.dto.BucketPolicyConfigDto;
import com.macro.mall.dto.MinioUploadDto;
import io.minio.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;


/**
 * seaweed 文件上传
 */
@Api(tags = "SeaweedController", description = "seaweed对象存储管理")
@Controller
@RequestMapping("/seaweed")
public class SeaweedFSController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeaweedFSController.class);
    @Resource
    private AmazonS3ClientHelper s3Client;

    @Value("${seaweed.server.bucket:pic_bucket}")
    private String bucket;

    @ApiOperation("文件上传")
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult upload(@RequestPart("file") MultipartFile file) {
        try {
            String fileUrl= s3Client.uploadObject(bucket,file);
            String objectName=fileUrl.substring(fileUrl.indexOf(bucket)+1);
            LOGGER.info("文件上传成功!"+fileUrl);
            MinioUploadDto minioUploadDto = new MinioUploadDto();
            minioUploadDto.setName(objectName);
            minioUploadDto.setUrl(fileUrl);
            return CommonResult.success(minioUploadDto);
        } catch (Exception e) {
            LOGGER.warn("上传发生错误", e);
        }
        return CommonResult.failed();
    }


    @ApiOperation("文件删除")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult delete(@Validated  @NotBlank @RequestParam("objectName") String objectName) {
        try {
            s3Client.deleteObject(bucket,objectName);
            LOGGER.info("delete file:{}",objectName);
            return CommonResult.success(null);
        } catch (Exception e) {
            LOGGER.warn("发生错误:！"+e.getMessage(),e);
        }
        return CommonResult.failed();
    }
}
