package com.example.foodshop.order.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "product_id"}),
       indexes = {
           @Index(name = "idx_cart_id", columnList = "cart_id"),
           @Index(name = "idx_product_id", columnList = "product_id")
       })
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
    
    public CartItem() {
    }
    
    public CartItem(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
        this.addedAt = LocalDateTime.now();
    }
    
    public CartItem(Long id, Cart cart, Long productId, Integer quantity, LocalDateTime addedAt) {
        this.id = id;
        this.cart = cart;
        this.productId = productId;
        this.quantity = quantity;
        this.addedAt = addedAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Cart getCart() {
        return cart;
    }
    
    public void setCart(Cart cart) {
        this.cart = cart;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public LocalDateTime getAddedAt() {
        return addedAt;
    }
    
    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
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
    
    public Integer getAvailableStock() {
        return availableStock;
    }
    
    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }
    
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