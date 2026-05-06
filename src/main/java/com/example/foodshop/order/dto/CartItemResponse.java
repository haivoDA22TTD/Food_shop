package com.example.foodshop.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private String productImage;
    private Integer quantity;
    private BigDecimal subtotal;
    private Integer availableStock;
    private LocalDateTime addedAt;
    private boolean inStock;
    
    public CartItemResponse(Long id, Long productId, String productName, BigDecimal productPrice, 
                           String productImage, Integer quantity, Integer availableStock, LocalDateTime addedAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productImage = productImage;
        this.quantity = quantity;
        this.availableStock = availableStock;
        this.addedAt = addedAt;
        
        // Calculate subtotal
        this.subtotal = productPrice != null && quantity != null 
                ? productPrice.multiply(BigDecimal.valueOf(quantity)) 
                : BigDecimal.ZERO;
        
        // Check if in stock
        this.inStock = availableStock != null && quantity <= availableStock;
    }
}