package com.example.foodshop.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Identity Service routes
                .route("identity-service", r -> r
                        .path("/api/auth/**", "/api/passkey/**")
                        .uri("lb://IDENTITY-SERVICE"))
                .build();
    }
}
