package com.example.foodshop.order.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders_ms", indexes = {
        @Index(name = "idx_orders_ms_user_id", columnList = "userId"),
        @Index(name = "idx_orders_ms_status", columnList = "status"),
        @Index(name = "idx_orders_ms_created_at", columnList = "createdAt")
})
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nullable to support guest checkout.
    private Long userId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItem> orderItems;

    @Column(nullable = false)
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private String shippingAddress;
    private String paymentMethod;
    private String voucherCode;
    private Double discountAmount;
    private Double originalAmount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = OrderStatus.PENDING;
        }
        if (discountAmount == null) {
            discountAmount = 0.0;
        }
    }

    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED
    }
}
