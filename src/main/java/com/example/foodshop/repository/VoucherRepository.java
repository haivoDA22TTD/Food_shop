package com.example.foodshop.repository;

import com.example.foodshop.entity.User;
import com.example.foodshop.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    
    Optional<Voucher> findByCode(String code);
    
    @Query("SELECT v FROM Voucher v WHERE v.code = :code AND v.isActive = true " +
           "AND v.expiresAt > :now AND v.usedCount < v.usageLimit")
    Optional<Voucher> findValidVoucherByCode(String code, LocalDateTime now);
    
    List<Voucher> findByUserAndIsActiveTrue(User user);
    
    @Query("SELECT v FROM Voucher v WHERE (v.user IS NULL OR v.user = :user) " +
           "AND v.isActive = true AND v.expiresAt > :now AND v.usedCount < v.usageLimit")
    List<Voucher> findAvailableVouchersForUser(User user, LocalDateTime now);
    
    @Query("SELECT v FROM Voucher v WHERE v.createdBy = 'AI_CHATBOT' " +
           "AND v.createdAt > :since ORDER BY v.createdAt DESC")
    List<Voucher> findRecentAIVouchers(LocalDateTime since);
}
