package com.example.foodshop.order.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderUserDetails {
    
    private Long userId;
    private String username;
    private String role;
    
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
    
    public boolean isUser() {
        return "USER".equals(role);
    }
}