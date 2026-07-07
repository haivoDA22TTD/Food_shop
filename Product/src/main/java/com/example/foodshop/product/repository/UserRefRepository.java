package com.example.foodshop.product.repository;

import com.example.foodshop.product.entity.UserRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRefRepository extends JpaRepository<UserRef, Long> {
    Optional<UserRef> findByUsername(String username);
}
