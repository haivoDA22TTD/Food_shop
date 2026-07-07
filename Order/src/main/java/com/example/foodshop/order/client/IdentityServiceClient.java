package com.example.foodshop.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign Client to communicate with Identity Service
 * Uses internal endpoint (/internal/users) which doesn't require authentication
 * This allows Order Service to fetch user information for displaying in admin panel
 */
@FeignClient(name = "IDENTITY-SERVICE", path = "/internal/users")
public interface IdentityServiceClient {
    
    /**
     * Get user information by user ID
     * @param id User ID
     * @return UserDTO with username, email, etc.
     */
    @GetMapping("/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}
