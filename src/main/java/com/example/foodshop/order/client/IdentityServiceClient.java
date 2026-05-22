package com.example.foodshop.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "IDENTITY-SERVICE", path = "/api/users")
public interface IdentityServiceClient {
    
    @GetMapping("/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}
