package com.example.foodshop.order.repository;

import com.example.foodshop.order.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<UserAddress> findByIdAndUserId(Long id, Long userId);
    long countByUserId(Long userId);
}
