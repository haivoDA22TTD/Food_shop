package com.example.foodshop.identity.controller;

import com.example.foodshop.identity.dto.UserDTO;
import com.example.foodshop.identity.entity.User;
import com.example.foodshop.identity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal API for microservices communication
 * No authentication required - only accessible within the microservices network
 * 
 * This controller provides endpoints for other services (Order, Payment, etc.)
 * to fetch user information without requiring JWT authentication.
 */
@RestController
@RequestMapping("/internal/users")
public class InternalUserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Get user by ID for internal microservices
     * Used by Order Service to display customer information in admin panel
     * 
     * @param id User ID
     * @return UserDTO with user information
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        try {
            User user = userService.findById(id);
            UserDTO userDTO = userService.toDTO(user);
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
