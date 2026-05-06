package com.example.foodshop.order.repository;

import com.example.foodshop.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    // Find order items by order ID
    List<OrderItem> findByOrderId(Long orderId);
    
    // Find order items by product ID
    List<OrderItem> findByProductId(Long productId);
    
    // Find order items by order ID and product ID
    List<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId);
    
    // Count order items by product ID
    long countByProductId(Long productId);
    
    // Get top selling products
    @Query("SELECT oi.productId, oi.productName, SUM(oi.quantity) as totalSold " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.productId, oi.productName " +
           "ORDER BY totalSold DESC")
    List<Object[]> findTopSellingProducts(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
    
    // Get product sales statistics
    @Query("SELECT oi.productId, COUNT(oi), SUM(oi.quantity), SUM(oi.subtotal) " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.productId")
    List<Object[]> getProductSalesStatistics(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);
    
    // Find order items by user ID (through order relationship)
    @Query("SELECT oi FROM OrderItem oi JOIN oi.order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<OrderItem> findByUserId(@Param("userId") Long userId);
    
    // Calculate total revenue for a product
    @Query("SELECT COALESCE(SUM(oi.subtotal), 0) FROM OrderItem oi WHERE oi.productId = :productId")
    Double calculateTotalRevenueByProductId(@Param("productId") Long productId);
}