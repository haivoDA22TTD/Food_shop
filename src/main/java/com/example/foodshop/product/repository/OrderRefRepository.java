package com.example.foodshop.product.repository;

import com.example.foodshop.product.entity.OrderRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRefRepository extends JpaRepository<OrderRef, Long> {
}
