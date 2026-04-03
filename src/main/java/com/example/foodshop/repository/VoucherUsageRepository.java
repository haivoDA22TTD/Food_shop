package com.example.foodshop.repository;

import com.example.foodshop.entity.User;
import com.example.foodshop.entity.Voucher;
import com.example.foodshop.entity.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {
    
    List<VoucherUsage> findByUser(User user);
    
    List<VoucherUsage> findByVoucher(Voucher voucher);
    
    boolean existsByVoucherAndUser(Voucher voucher, User user);
}
