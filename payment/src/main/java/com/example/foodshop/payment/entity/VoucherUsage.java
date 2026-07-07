package com.example.foodshop.payment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher_usages", indexes = {
        @Index(name = "idx_voucher_user", columnList = "voucher_id, user_id"),
        @Index(name = "idx_payment", columnList = "payment_id"),
        @Index(name = "idx_used_at", columnList = "used_at")
})
public class VoucherUsage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "voucher_id", nullable = false)
    private Long voucherId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "payment_id")
    private Long paymentId;
    
    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;
    
    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;
    
    // Constructors
    public VoucherUsage() {
    }
    
    public VoucherUsage(Long voucherId, Long userId, Long paymentId, BigDecimal discountAmount) {
        this.voucherId = voucherId;
        this.userId = userId;
        this.paymentId = paymentId;
        this.discountAmount = discountAmount;
        this.usedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getVoucherId() {
        return voucherId;
    }
    
    public void setVoucherId(Long voucherId) {
        this.voucherId = voucherId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public LocalDateTime getUsedAt() {
        return usedAt;
    }
    
    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }
    
    @PrePersist
    protected void onCreate() {
        if (usedAt == null) {
            usedAt = LocalDateTime.now();
        }
    }
}
