package com.exam;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(ExamProps.class)
public class config {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
