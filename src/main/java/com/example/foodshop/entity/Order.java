package com.example.foodshop.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "createdAt"),
    @Index(name = "idx_voucher_code", columnList = "voucherCode")
})
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<OrderItem> orderItems;
    
    @Column(nullable = false)
    private Double totalAmount;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    private String shippingAddress;
    private String paymentMethod;
    
    // Voucher fields
    private String voucherCode;
    private Double discountAmount;
    private Double originalAmount; // Tổng tiền trước khi giảm giá
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = OrderStatus.PENDING;
    }
    
    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED
    }
}
