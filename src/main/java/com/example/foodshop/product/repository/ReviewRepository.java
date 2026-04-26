package com.example.foodshop.product.repository;

import com.example.foodshop.product.entity.Product;
import com.example.foodshop.product.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct(Product product);

    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.product = :product ORDER BY r.createdAt DESC")
    List<Review> findByProductWithUser(@Param("product") Product product);
}
