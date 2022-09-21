package com.macro.mall.common.domain;


import com.amazonaws.Protocol;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = AmazonS3Properties.amazonS3Prefix)
@Data
public class AmazonS3Properties {
    public static final String amazonS3Prefix = "seaweed.client.amazons3";

    private String accessKey;
    private String secretKey;
    /**
     * remote gateway server address info
     */
    private Endpoint endpoint;

    private ClientConfig config= ClientConfig.DEFAULT;


    @Data
    public static class Endpoint {
        /**
         * e.g. https://sns.us-west-1.amazonaws.com or sns.us-west-1.amazonaws.com
         **/
        private String serverUri;
        /**
         * the region to use for SigV4 signing of requests (e.g. us-west-1)
         */
        private String region;

    }

    @Data
    public static class ClientConfig {
        static final ClientConfig DEFAULT=new ClientConfig();

        private Protocol protocol = Protocol.HTTP;
        private Integer maxConnections;
        private Integer connectionTimeoutMills;
        private Integer readTimeoutMills;
        private Integer connectionMaxIdleMillis;

    }

}
