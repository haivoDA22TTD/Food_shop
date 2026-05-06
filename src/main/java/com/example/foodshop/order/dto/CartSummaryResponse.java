package com.example.foodshop.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartSummaryResponse {
    
    private Integer totalItems;
    private BigDecimal totalAmount;
    private boolean hasItems;
    
    public CartSummaryResponse(Integer totalItems, BigDecimal totalAmount) {
        this.totalItems = totalItems != null ? totalItems : 0;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.hasItems = this.totalItems > 0;
    }
}