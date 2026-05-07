package com.example.foodshop.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CartResponse {
    
    private Long id;
    private Long userId;
    private List<CartItemResponse> cartItems;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private LocalDateTime updatedAt;
    
    // Constructors
    public CartResponse() {
    }
    
    public CartResponse(Long id, Long userId, List<CartItemResponse> cartItems, BigDecimal totalAmount,
                       Integer totalItems, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.cartItems = cartItems;
        this.totalAmount = totalAmount;
        this.totalItems = totalItems;
        this.updatedAt = updatedAt;
    }
    
    public CartResponse(Long id, Long userId, List<CartItemResponse> cartItems, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.cartItems = cartItems;
        this.updatedAt = updatedAt;
        
        // Calculate totals
        this.totalAmount = cartItems.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.totalItems = cartItems.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public List<CartItemResponse> getCartItems() {
        return cartItems;
    }
    
    public void setCartItems(List<CartItemResponse> cartItems) {
        this.cartItems = cartItems;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public Integer getTotalItems() {
        return totalItems;
    }
    
    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}