package com.example.foodshop.payment.repository;

import com.example.foodshop.payment.entity.Voucher;
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
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    
    Optional<Voucher> findByCode(String code);
    
    Optional<Voucher> findByCodeAndIsActiveTrue(String code);
    
    Page<Voucher> findByIsActiveTrueAndValidFromBeforeAndValidToAfterOrderByCreatedAtDesc(
            LocalDateTime now1, LocalDateTime now2, Pageable pageable);
    
    @Query("SELECT v FROM Voucher v WHERE v.isActive = true AND " +
           "v.validFrom <= :now AND v.validTo >= :now AND " +
           "(v.usageLimit IS NULL OR v.usageCount < v.usageLimit)")
    List<Voucher> findAvailableVouchers(@Param("now") LocalDateTime now);
    
    @Query("SELECT v FROM Voucher v WHERE v.code LIKE %:keyword% OR v.name LIKE %:keyword% " +
           "ORDER BY v.createdAt DESC")
    Page<Voucher> searchVouchers(@Param("keyword") String keyword, Pageable pageable);
    
    List<Voucher> findByIsActiveTrueAndValidToBeforeAndUpdatedAtBefore(
            LocalDateTime validTo, LocalDateTime updatedAt);
}
