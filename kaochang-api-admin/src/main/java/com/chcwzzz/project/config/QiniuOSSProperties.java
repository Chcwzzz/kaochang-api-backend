package com.chcwzzz.project.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "qiniuyun.oss")
public class QiniuOSSProperties {
    private String accessKey;
    private String secretKey;
    private String bucket;
}