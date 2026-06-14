package com.pms.notificationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import java.security.SecureRandom;

@Configuration
@EnableRetry
public class RetryConfig {

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
}
