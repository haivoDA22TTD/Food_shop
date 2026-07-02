package com.example.foodshop.product.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class UserRef {
    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String email;
}
