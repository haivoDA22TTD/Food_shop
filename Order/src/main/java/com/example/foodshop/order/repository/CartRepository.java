package com.example.foodshop.order.repository;

import com.example.foodshop.order.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    // Find cart by user ID
    Optional<Cart> findByUserId(Long userId);
    
    // Check if cart exists for user
    boolean existsByUserId(Long userId);
    
    // Delete cart by user ID
    void deleteByUserId(Long userId);
    
    // Find carts that haven't been updated for a while (for cleanup)
    @Query("SELECT c FROM Cart c WHERE c.updatedAt < :cutoffTime")
    List<Cart> findCartsNotUpdatedSince(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Count total items in user's cart
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM Cart c JOIN c.cartItems ci WHERE c.userId = :userId")
    Integer countTotalItemsByUserId(@Param("userId") Long userId);
    
    // Get cart with items for user
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems WHERE c.userId = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);
    
    // Delete empty carts (carts with no items)
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.id NOT IN (SELECT DISTINCT ci.cart.id FROM CartItem ci)")
    int deleteEmptyCarts();
}