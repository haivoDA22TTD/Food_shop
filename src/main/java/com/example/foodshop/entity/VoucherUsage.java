package com.example.foodshop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher_usage")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherUsage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;
    
    @Column(nullable = false)
    private LocalDateTime usedAt = LocalDateTime.now();
}
