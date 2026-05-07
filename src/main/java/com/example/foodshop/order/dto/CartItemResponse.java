package com.example.foodshop.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    
    // Constructors
    public CartItemResponse() {
    }
    
    public CartItemResponse(Long id, Long productId, String productName, BigDecimal productPrice,
                           String productImage, Integer quantity, BigDecimal subtotal, Integer availableStock,
                           LocalDateTime addedAt, boolean inStock) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productImage = productImage;
        this.quantity = quantity;
        this.subtotal = subtotal;
        this.availableStock = availableStock;
        this.addedAt = addedAt;
        this.inStock = inStock;
    }
    
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
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public BigDecimal getProductPrice() {
        return productPrice;
    }
    
    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }
    
    public String getProductImage() {
        return productImage;
    }
    
    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public Integer getAvailableStock() {
        return availableStock;
    }
    
    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }
    
    public LocalDateTime getAddedAt() {
        return addedAt;
    }
    
    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
    
    public boolean isInStock() {
        return inStock;
    }
    
    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }
}