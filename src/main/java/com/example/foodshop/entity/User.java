package com.example.foodshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String email;
    
    private String phone;
    private String address;
    
    @Enumerated(EnumType.STRING)
    private Role role;
    
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private Integer cancelledOrdersThisMonth = 0;
    
    private LocalDateTime lastCancelledAt;
    
    @Column(nullable = false)
    private Boolean accountLocked = false;
    
    private String lockReason;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (cancelledOrdersThisMonth == null) {
            cancelledOrdersThisMonth = 0;
        }
        if (accountLocked == null) {
            accountLocked = false;
        }
    }
    
    public enum Role {
        ADMIN, CUSTOMER
    }
}
