package com.example.foodshop.order.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "product_id"}),
       indexes = {
           @Index(name = "idx_cart_id", columnList = "cart_id"),
           @Index(name = "idx_product_id", columnList = "product_id")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonBackReference
    private Cart cart;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "added_at")
    private LocalDateTime addedAt;
    
    // Cached product info for performance (loaded from Product Service)
    @Transient
    private String productName;
    
    @Transient
    private BigDecimal productPrice;
    
    @Transient
    private String productImage;
    
    @Transient
    private Integer availableStock;
    
    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
    
    public BigDecimal getSubtotal() {
        if (productPrice != null && quantity != null) {
            return productPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
    
    public CartItem(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
        this.addedAt = LocalDateTime.now();
    }
    
    public void updateQuantity(Integer newQuantity) {
        this.quantity = newQuantity;
    }
    
    public boolean isValidQuantity() {
        return quantity != null && quantity > 0;
    }
    
    public boolean hasEnoughStock() {
        return availableStock != null && quantity <= availableStock;
    }
}