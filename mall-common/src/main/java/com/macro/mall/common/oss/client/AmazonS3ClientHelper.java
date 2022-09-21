package com.macro.mall.common.oss.client;

import com.alibaba.fastjson2.JSON;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author shangcj
 * @version 1.0
 * @description
 * @date 2022/8/18 17:10:37
 */

/**
 * 帮助类，业务类直接注入此类实现对象上传
 */
@ConditionalOnBean(AmazonS3.class)
@Component
@Slf4j
public class AmazonS3ClientHelper {

	@Resource
	private AmazonS3 amazonS3;

	@Value("${seaweed.server.publicUrl}")
	private String publicURL;

	/**
	 * 上传文件
	 * @param bucketName 桶名称
	 * @param fileKey 文件在桶内唯一标识
	 * @param file
	 * @throws IOException
	 */
	@SneakyThrows
	public void uploadObject(String bucketName, String fileKey, File file){
		amazonS3.putObject(bucketName,fileKey,file);
	}

	/**
	 * 自动生成fileKey，规则：month/sha256Hex(file content bytes)
	 *
	 * @param bucketName
	 * @param file
	 * @return 文件url例子：
	 *
	 **/
	@SneakyThrows
	public String uploadObject(String bucketName, MultipartFile file){
		String fileName = file.getOriginalFilename();
		String fileExt=fileName.substring(fileName.lastIndexOf("."));
		String fileKey=getMonth()+"/"+ DigestUtils.sha1Hex(file.getBytes())+fileExt;
		return uploadObject(bucketName,fileKey,file);
	}

	/**
	 *
	 * @param bucketName
	 * @param fileKey 可由业务单号+原始文件名拼成
	 * @param file
	 * @return
	 */
	@SneakyThrows
	public String uploadObject(String bucketName, String fileKey, MultipartFile file){
		return uploadObject(bucketName,fileKey,file,null);
	}

	/**
	 * 对象上传
	 * @param bucketName 桶名称
	 * @param fileKey -文件在桶内唯一标识
	 * @param file -文件
	 * @param fileMetaData -文件元数据
	 * @throws IOException
	 */
	public String uploadObject(String bucketName, String fileKey, MultipartFile file, Map<String,String> fileMetaData) throws IOException {
		ObjectMetadata objectMetadata = new ObjectMetadata();
		InputStream inputStream=file.getInputStream();
		objectMetadata.setContentType(file.getContentType());
		objectMetadata.setContentLength(inputStream.available());
//		objectMetadata.setContentLength(file.getSize());
		if(!CollectionUtils.isEmpty(fileMetaData)){
			objectMetadata.setUserMetadata(fileMetaData);
		}

		PutObjectResult result = amazonS3.putObject(bucketName, fileKey, inputStream, objectMetadata);

		log.info("bucketName:{} fileKey:{} uploadRes:{}",bucketName,fileKey, JSON.toJSONString(result));

		//TODO 生成url,最大有效期只有七天，因此通过 url组装方式返回
//		long longlongExpired=System.currentTimeMillis()+3600*24*1000*7;
//		URL url= amazonS3.generatePresignedUrl(bucketName,fileKey, Date.from(Instant.ofEpochMilli(longlongExpired)));
		return publicURL+"/"+bucketName+"/"+fileKey;
	}

	public void deleteObject(String bucketName,String fileKey){
		amazonS3.deleteObject(bucketName,fileKey);
	}

	private String getMonth(){
		LocalDate now=LocalDate.now();
		return DateTimeFormatter.ofPattern("yyyyMM").format(now);
	}

}
