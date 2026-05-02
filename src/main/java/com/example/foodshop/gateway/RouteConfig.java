package com.example.foodshop.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Value("${app.gateway.identity-uri:lb://IDENTITY-SERVICE}")
    private String identityServiceUri;
    
    @Value("${app.gateway.order-uri:lb://ORDER-SERVICE}")
    private String orderServiceUri;
    
    @Value("${app.gateway.product-uri:lb://PRODUCT-SERVICE}")
    private String productServiceUri;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Identity Service routes
                .route("identity-service", r -> r
                        .path("/api/auth/**", "/api/passkey/**", "/api/users/**", "/oauth2/**", "/login/oauth2/**")
                        .filters(f -> f
                                .retry(config -> config
                                        .setRetries(2)
                                        .setMethods(org.springframework.http.HttpMethod.GET, org.springframework.http.HttpMethod.POST)
                                        .setStatuses(org.springframework.http.HttpStatus.BAD_GATEWAY,
                                                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                                                org.springframework.http.HttpStatus.GATEWAY_TIMEOUT))
                                .circuitBreaker(config -> config
                                        .setName("authServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/auth")))
                        .uri(identityServiceUri))
                // Order Service routes
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .retry(config -> config
                                        .setRetries(2)
                                        .setMethods(org.springframework.http.HttpMethod.GET, org.springframework.http.HttpMethod.POST)
                                        .setStatuses(org.springframework.http.HttpStatus.BAD_GATEWAY,
                                                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                                                org.springframework.http.HttpStatus.GATEWAY_TIMEOUT))
                                .circuitBreaker(config -> config
                                        .setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/order")))
                        .uri(orderServiceUri))
                // Product Service routes
                .route("product-service", r -> r
                        .path("/api/products/**", "/api/admin/products/**", "/api/reviews/**", "/api/chatbot/**", "/api/upload/**")
                        .filters(f -> f
                                .retry(config -> config
                                        .setRetries(2)
                                        .setMethods(org.springframework.http.HttpMethod.GET, org.springframework.http.HttpMethod.POST)
                                        .setStatuses(org.springframework.http.HttpStatus.BAD_GATEWAY,
                                                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                                                org.springframework.http.HttpStatus.GATEWAY_TIMEOUT))
                                .circuitBreaker(config -> config
                                        .setName("productServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/product")))
                        .uri(productServiceUri))
                .build();
    }
}
