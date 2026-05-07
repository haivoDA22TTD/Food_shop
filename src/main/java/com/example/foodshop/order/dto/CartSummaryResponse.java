package com.example.foodshop.order.dto;

import java.math.BigDecimal;

public class CartSummaryResponse {
    
    private Integer totalItems;
    private BigDecimal totalAmount;
    private boolean hasItems;
    
    // Constructors
    public CartSummaryResponse() {
    }
    
    public CartSummaryResponse(Integer totalItems, BigDecimal totalAmount, boolean hasItems) {
        this.totalItems = totalItems;
        this.totalAmount = totalAmount;
        this.hasItems = hasItems;
    }
    
    public CartSummaryResponse(Integer totalItems, BigDecimal totalAmount) {
        this.totalItems = totalItems != null ? totalItems : 0;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.hasItems = this.totalItems > 0;
    }
    
    // Getters and Setters
    public Integer getTotalItems() {
        return totalItems;
    }
    
    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public boolean isHasItems() {
        return hasItems;
    }
    
    public void setHasItems(boolean hasItems) {
        this.hasItems = hasItems;
    }
}