package com.example.foodshop.order.repository;

import com.example.foodshop.order.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    // Find cart item by cart ID and product ID
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    
    // Find all cart items by cart ID
    List<CartItem> findByCartIdOrderByAddedAtDesc(Long cartId);
    
    // Find cart items by product ID (across all carts)
    List<CartItem> findByProductId(Long productId);
    
    // Delete cart item by cart ID and product ID
    void deleteByCartIdAndProductId(Long cartId, Long productId);
    
    // Delete all cart items by cart ID
    void deleteByCartId(Long cartId);
    
    // Count cart items by cart ID
    int countByCartId(Long cartId);
    
    // Sum quantities by cart ID
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Integer sumQuantitiesByCartId(@Param("cartId") Long cartId);
    
    // Check if product exists in any cart
    boolean existsByProductId(Long productId);
    
    // Find cart items by user ID (through cart relationship)
    @Query("SELECT ci FROM CartItem ci JOIN ci.cart c WHERE c.userId = :userId ORDER BY ci.addedAt DESC")
    List<CartItem> findByUserId(@Param("userId") Long userId);
    
    // Delete cart items by product ID (when product is deleted)
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.productId = :productId")
    int deleteByProductId(@Param("productId") Long productId);
}