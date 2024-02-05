package com.chcwzzz.sdk.config;

import com.chcwzzz.sdk.client.KaochangClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "kaochang.client")
public class KaochangClientConfig {
    private String accessKey;
    private String secretKey;

    @Bean
    public KaochangClient kaochangClient() {
        return new KaochangClient(accessKey, secretKey);
    }
}
