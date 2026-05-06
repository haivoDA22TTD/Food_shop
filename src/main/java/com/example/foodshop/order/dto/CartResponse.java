package com.example.foodshop.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    
    private Long id;
    private Long userId;
    private List<CartItemResponse> cartItems;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private LocalDateTime updatedAt;
    
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
}