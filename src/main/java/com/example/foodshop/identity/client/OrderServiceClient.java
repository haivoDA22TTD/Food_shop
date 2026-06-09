package com.example.foodshop.identity.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Client for communicating with Order Service to create shipper profiles.
 * Implements retry logic with exponential backoff for connection errors.
 */
@Component
@Slf4j
public class OrderServiceClient {

    private final RestTemplate restTemplate;
    private final String orderServiceUrl;
    
    // Retry configuration
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 1000; // 1 second
    
    public OrderServiceClient(
            RestTemplate restTemplate,
            @Value("${order.service.url:http://localhost:8082}") String orderServiceUrl) {
        this.restTemplate = restTemplate;
        this.orderServiceUrl = orderServiceUrl;
    }
    
    /**
     * Creates a shipper profile in the Order Service.
     * 
     * @param request The shipper profile creation request
     * @return The created shipper profile response
     * @throws OrderServiceException if shipper profile creation fails
     */
    public ShipperResponse createShipperProfile(CreateShipperProfileRequest request) 
            throws OrderServiceException {
        
        String url = orderServiceUrl + "/internal/shippers";
        
        log.info("Creating shipper profile in Order Service for userId: {}", request.getUserId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateShipperProfileRequest> entity = new HttpEntity<>(request, headers);
        
        int attempt = 0;
        long backoffMs = INITIAL_BACKOFF_MS;
        
        while (attempt < MAX_RETRIES) {
            try {
                ResponseEntity<ShipperResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ShipperResponse.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    log.info("Successfully created shipper profile for userId: {}", request.getUserId());
                    return response.getBody();
                }
                
                throw new OrderServiceException(
                    "Order Service returned unsuccessful status: " + response.getStatusCode()
                );
                
            } catch (HttpClientErrorException e) {
                // 4xx errors - don't retry, these are client errors
                log.error("Client error creating shipper profile: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
                throw new OrderServiceException(
                    "Failed to create shipper profile: " + e.getResponseBodyAsString(),
                    e
                );
                
            } catch (HttpServerErrorException e) {
                // 5xx errors - retry with exponential backoff
                attempt++;
                log.warn("Server error creating shipper profile (attempt {}/{}): {} - {}", 
                    attempt, MAX_RETRIES, e.getStatusCode(), e.getResponseBodyAsString());
                
                if (attempt >= MAX_RETRIES) {
                    throw new OrderServiceException(
                        "Failed to create shipper profile after " + MAX_RETRIES + 
                        " attempts: " + e.getResponseBodyAsString(),
                        e
                    );
                }
                
                sleep(backoffMs);
                backoffMs *= 2; // Exponential backoff
                
            } catch (ResourceAccessException e) {
                // Connection errors - retry with exponential backoff
                attempt++;
                log.warn("Connection error creating shipper profile (attempt {}/{}): {}", 
                    attempt, MAX_RETRIES, e.getMessage());
                
                if (attempt >= MAX_RETRIES) {
                    throw new OrderServiceException(
                        "Failed to connect to Order Service after " + MAX_RETRIES + 
                        " attempts. Service may be unavailable.",
                        e
                    );
                }
                
                sleep(backoffMs);
                backoffMs *= 2; // Exponential backoff
                
            } catch (Exception e) {
                // Unexpected errors - don't retry
                log.error("Unexpected error creating shipper profile: {}", e.getMessage(), e);
                throw new OrderServiceException(
                    "Unexpected error creating shipper profile: " + e.getMessage(),
                    e
                );
            }
        }
        
        throw new OrderServiceException(
            "Failed to create shipper profile after " + MAX_RETRIES + " attempts"
        );
    }
    
    /**
     * Sleep helper method for retry backoff.
     */
    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OrderServiceException("Retry interrupted", e);
        }
    }
    
    /**
     * Request DTO for creating shipper profile in Order Service.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateShipperProfileRequest {
        private Long userId;
        private String name;
        private String phone;
        private String email;
        private String vehicleType;
        private String vehicleNumber;
    }
    
    /**
     * Response DTO from Order Service after creating shipper profile.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShipperResponse {
        private Long id;
        private Long userId;
        private String name;
        private String phone;
        private String email;
        private String status;
        private String vehicleType;
        private String vehicleNumber;
        private Integer totalDeliveries;
        private Integer successfulDeliveries;
        private Double rating;
        private Boolean isActive;
        private String createdAt;
    }
    
    /**
     * Custom exception for Order Service communication errors.
     */
    public static class OrderServiceException extends RuntimeException {
        public OrderServiceException(String message) {
            super(message);
        }
        
        public OrderServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
