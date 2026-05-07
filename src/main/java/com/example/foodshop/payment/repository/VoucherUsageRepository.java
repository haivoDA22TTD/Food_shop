package com.example.foodshop.payment.repository;

import com.example.foodshop.payment.entity.VoucherUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {
    
    int countByVoucherIdAndUserId(Long voucherId, Long userId);
    
    List<VoucherUsage> findByVoucherIdOrderByUsedAtDesc(Long voucherId);
    
    Page<VoucherUsage> findByVoucherIdOrderByUsedAtDesc(Long voucherId, Pageable pageable);
    
    List<VoucherUsage> findByUserIdOrderByUsedAtDesc(Long userId);
    
    @Query("SELECT vu FROM VoucherUsage vu WHERE vu.voucherId = :voucherId AND " +
           "vu.usedAt BETWEEN :startDate AND :endDate ORDER BY vu.usedAt DESC")
    List<VoucherUsage> findVoucherUsageInDateRange(@Param("voucherId") Long voucherId,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(vu) FROM VoucherUsage vu WHERE vu.voucherId = :voucherId")
    long countByVoucherId(@Param("voucherId") Long voucherId);
    
    @Query("SELECT SUM(vu.discountAmount) FROM VoucherUsage vu WHERE vu.voucherId = :voucherId")
    java.math.BigDecimal getTotalDiscountByVoucher(@Param("voucherId") Long voucherId);
}
