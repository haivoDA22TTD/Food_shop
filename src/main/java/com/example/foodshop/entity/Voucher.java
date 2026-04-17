package com.example.foodshop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers", indexes = {
    @Index(name = "idx_code", columnList = "code"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_expires_at", columnList = "expiresAt"),
    @Index(name = "idx_active_expires", columnList = "isActive, expiresAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String code;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal minOrderValue = BigDecimal.ZERO;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal maxDiscount;
    
    @Column(nullable = false)
    private Integer usageLimit = 1;
    
    @Column(nullable = false)
    private Integer usedCount = 0;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // NULL = public voucher, có giá trị = personal voucher
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(length = 50)
    private String createdBy = "AI_CHATBOT";
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(length = 500)
    private String description;
    
    public enum DiscountType {
        PERCENTAGE, // Giảm theo %
        FIXED       // Giảm số tiền cố định
    }
    
    // Helper methods
    public boolean isValid() {
        return isActive 
            && LocalDateTime.now().isBefore(expiresAt)
            && usedCount < usageLimit;
    }
    
    public boolean canUse(User user) {
        if (!isValid()) return false;
        if (this.user == null) return true; // Public voucher
        return this.user.getId().equals(user.getId()); // Personal voucher
    }
    
    public BigDecimal calculateDiscount(BigDecimal orderTotal) {
        if (orderTotal.compareTo(minOrderValue) < 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discount;
        if (discountType == DiscountType.PERCENTAGE) {
            discount = orderTotal.multiply(discountValue).divide(BigDecimal.valueOf(100));
        } else {
            discount = discountValue;
        }
        
        // Apply max discount if set
        if (maxDiscount != null && discount.compareTo(maxDiscount) > 0) {
            discount = maxDiscount;
        }
        
        return discount;
    }
}
