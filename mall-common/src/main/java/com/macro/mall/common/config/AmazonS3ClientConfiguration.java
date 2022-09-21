package com.macro.mall.common.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.macro.mall.common.domain.AmazonS3Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
@EnableConfigurationProperties(AmazonS3Properties.class)
@ConditionalOnProperty(prefix = AmazonS3Properties.amazonS3Prefix,value = "enabled",havingValue = "true")
public class AmazonS3ClientConfiguration {


	/**
	 * 代码中直接注入，AmazonS3 bean即可，其中文件上传的方法可参考阿里云oss OSSClient 客户端
	 * 方法签名与AmazonS3相似度99%
	 * 或者到AmazonS3(simple storage service)
	 * 官网查看相关使用：https://docs.ceph.com/en/quincy/radosgw/s3/java/
	 * userguide 地址：https://docs.aws.amazon.com/AmazonS3/latest/userguide/tutorials.html
	 * @param s3Properties
	 * @return
	 */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public AmazonS3 buildAmazonS3Client(AmazonS3Properties s3Properties){
        AWSCredentials credentials = new BasicAWSCredentials(s3Properties.getAccessKey(), s3Properties.getSecretKey());

        Protocol configProtocol=s3Properties.getConfig().getProtocol();
        Integer maxConnections=s3Properties.getConfig().getMaxConnections();
        Integer connectionTimeoutMills=s3Properties.getConfig().getConnectionTimeoutMills();
        Integer readTimeoutMills=s3Properties.getConfig().getReadTimeoutMills();
        Integer connectionMaxIdleMillis=s3Properties.getConfig().getConnectionMaxIdleMillis();

        ClientConfiguration clientConfig = new ClientConfiguration()
                .withProtocol(configProtocol==null?Protocol.HTTP:configProtocol);
        if(checkPositive(maxConnections)){
            clientConfig.withMaxConnections(maxConnections);
        }
        if(checkPositive(connectionTimeoutMills)){
            clientConfig.withConnectionTimeout(connectionTimeoutMills);
        }
        if(checkPositive(readTimeoutMills)){
            clientConfig.withSocketTimeout(readTimeoutMills);
        }
        if(checkPositive(connectionMaxIdleMillis)){
            clientConfig.withConnectionMaxIdleMillis(connectionMaxIdleMillis);
        }

        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(s3Properties.getEndpoint().getServerUri()
                        , s3Properties.getEndpoint().getRegion()))
                .withClientConfiguration(clientConfig)
                .build();
    }
    private boolean checkPositive(Integer param){
        return Objects.nonNull(param) && param.intValue()>0;
    }
}
