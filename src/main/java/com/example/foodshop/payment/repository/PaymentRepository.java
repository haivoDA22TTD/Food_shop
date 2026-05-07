package com.example.foodshop.payment.repository;

import com.example.foodshop.payment.entity.Payment;
import com.example.foodshop.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByPaymentNumber(String paymentNumber);
    
    Optional<Payment> findByOrderId(Long orderId);
    
    Optional<Payment> findByOrderIdAndPaymentStatusIn(Long orderId, List<PaymentStatus> statuses);
    
    Page<Payment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    Page<Payment> findByUserIdAndPaymentStatusOrderByCreatedAtDesc(Long userId, PaymentStatus status, Pageable pageable);
    
    Page<Payment> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus status, Pageable pageable);
    
    List<Payment> findByPaymentStatusAndExpiredAtBefore(PaymentStatus status, LocalDateTime expiredAt);
    
    @Query("SELECT p FROM Payment p WHERE p.paymentNumber LIKE %:keyword% OR " +
           "CAST(p.orderId AS string) LIKE %:keyword% ORDER BY p.createdAt DESC")
    Page<Payment> searchPayments(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT p.paymentStatus as status, COUNT(p) as count, SUM(p.finalAmount) as totalAmount " +
           "FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY p.paymentStatus")
    List<Object[]> getPaymentStatistics(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.userId = :userId AND p.paymentStatus = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") PaymentStatus status);
    
    @Query("SELECT SUM(p.finalAmount) FROM Payment p WHERE p.userId = :userId AND p.paymentStatus = 'COMPLETED'")
    Optional<java.math.BigDecimal> getTotalSpentByUser(@Param("userId") Long userId);
}
