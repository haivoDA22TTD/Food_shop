package com.example.foodshop.payment.security;

public class PaymentUserDetails {
    
    private Long userId;
    private String username;
    private String role;
    
    public PaymentUserDetails() {
    }
    
    public PaymentUserDetails(Long userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
    
    public boolean isUser() {
        return "USER".equals(role);
    }
}
