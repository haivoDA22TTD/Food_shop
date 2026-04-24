package com.example.foodshop.order.repository;

import com.example.foodshop.order.entity.ProductRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRefRepository extends JpaRepository<ProductRef, Long> {
}
