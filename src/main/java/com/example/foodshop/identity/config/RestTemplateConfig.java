package com.example.foodshop.identity.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for RestTemplate bean
 * Used for inter-service communication (e.g., Identity Service -> Order Service)
 * @LoadBalanced cho phép dùng "lb://ORDER-SERVICE" để resolve qua Eureka
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}