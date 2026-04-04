package com.example.foodshop.service;

import com.example.foodshop.entity.User;
import com.example.foodshop.entity.Voucher;
import com.example.foodshop.entity.VoucherUsage;
import com.example.foodshop.repository.VoucherRepository;
import com.example.foodshop.repository.VoucherUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherService {
    
    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final Random random = new Random();
    
    /**
     * Tạo voucher chào mừng cho khách hàng mới
     */
    @Transactional
    public Voucher createWelcomeVoucher(User user) {
        String code = generateVoucherCode("WELCOME");
        
        Voucher voucher = new Voucher();
        voucher.setCode(code);
        voucher.setDiscountType(Voucher.DiscountType.FIXED);
        voucher.setDiscountValue(BigDecimal.valueOf(10000)); // Giảm 10k
        voucher.setMinOrderValue(BigDecimal.valueOf(50000)); // Đơn tối thiểu 50k
        voucher.setUsageLimit(1);
        voucher.setUser(user); // Personal voucher
        voucher.setExpiresAt(LocalDateTime.now().plusDays(7)); // Hết hạn sau 7 ngày
        voucher.setCreatedBy("AI_CHATBOT");
        voucher.setDescription("Mã giảm giá chào mừng khách hàng mới");
        
        return voucherRepository.save(voucher);
    }
    
    /**
     * Tạo voucher comeback cho khách hàng quay lại
     */
    @Transactional
    public Voucher createComebackVoucher(User user) {
        String code = generateVoucherCode("COMEBACK");
        
        Voucher voucher = new Voucher();
        voucher.setCode(code);
        voucher.setDiscountType(Voucher.DiscountType.PERCENTAGE);
        voucher.setDiscountValue(BigDecimal.valueOf(15)); // Giảm 15%
        voucher.setMinOrderValue(BigDecimal.valueOf(100000)); // Đơn tối thiểu 100k
        voucher.setMaxDiscount(BigDecimal.valueOf(30000)); // Giảm tối đa 30k
        voucher.setUsageLimit(1);
        voucher.setUser(user);
        voucher.setExpiresAt(LocalDateTime.now().plusHours(24)); // Hết hạn sau 24h
        voucher.setCreatedBy("AI_CHATBOT");
        voucher.setDescription("Mã giảm giá cho khách hàng quay lại");
        
        return voucherRepository.save(voucher);
    }
    
    /**
     * Tạo voucher VIP cho khách hàng thân thiết
     */
    @Transactional
    public Voucher createVIPVoucher(User user) {
        String code = generateVoucherCode("VIP");
        
        Voucher voucher = new Voucher();
        voucher.setCode(code);
        voucher.setDiscountType(Voucher.DiscountType.PERCENTAGE);
        voucher.setDiscountValue(BigDecimal.valueOf(20)); // Giảm 20%
        voucher.setMinOrderValue(BigDecimal.valueOf(150000)); // Đơn tối thiểu 150k
        voucher.setMaxDiscount(BigDecimal.valueOf(50000)); // Giảm tối đa 50k
        voucher.setUsageLimit(3); // Dùng được 3 lần
        voucher.setUser(user);
        voucher.setExpiresAt(LocalDateTime.now().plusDays(30)); // Hết hạn sau 30 ngày
        voucher.setCreatedBy("AI_CHATBOT");
        voucher.setDescription("Mã giảm giá VIP cho khách hàng thân thiết");
        
        return voucherRepository.save(voucher);
    }
    
    /**
     * Tạo voucher deal đặc biệt (khi khách hàng thương lượng)
     */
    @Transactional
    public Voucher createDealVoucher(User user) {
        String code = generateVoucherCode("DEAL");
        
        Voucher voucher = new Voucher();
        voucher.setCode(code);
        voucher.setDiscountType(Voucher.DiscountType.PERCENTAGE);
        voucher.setDiscountValue(BigDecimal.valueOf(10)); // Giảm 10%
        voucher.setMinOrderValue(BigDecimal.ZERO); // Không yêu cầu tối thiểu
        voucher.setMaxDiscount(BigDecimal.valueOf(20000)); // Giảm tối đa 20k
        voucher.setUsageLimit(1);
        voucher.setUser(user);
        voucher.setExpiresAt(LocalDateTime.now().plusHours(1)); // Hết hạn sau 1h
        voucher.setCreatedBy("AI_CHATBOT");
        voucher.setDescription("Mã giảm giá đặc biệt từ AI");
        
        return voucherRepository.save(voucher);
    }
    
    /**
     * Validate và apply voucher
     */
    public BigDecimal applyVoucher(String code, BigDecimal orderTotal, User user) {
        Voucher voucher = voucherRepository.findValidVoucherByCode(code, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ hoặc đã hết hạn"));
        
        if (!voucher.canUse(user)) {
            throw new RuntimeException("Bạn không thể sử dụng mã giảm giá này");
        }
        
        if (orderTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new RuntimeException("Đơn hàng tối thiểu " + voucher.getMinOrderValue() + "đ để sử dụng mã này");
        }
        
        return voucher.calculateDiscount(orderTotal);
    }
    
    /**
     * Mark voucher as used (increment usedCount + save to voucher_usage)
     */
    @Transactional
    public void markVoucherAsUsed(String code, User user, com.example.foodshop.entity.Order order, double discountAmount) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        
        // Increment used count
        voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);
        
        // Save to voucher_usage for tracking
        VoucherUsage usage = new VoucherUsage();
        usage.setVoucher(voucher);
        usage.setUser(user);
        usage.setOrder(order);
        usage.setDiscountAmount(BigDecimal.valueOf(discountAmount));
        usage.setUsedAt(LocalDateTime.now());
        voucherUsageRepository.save(usage);
        
        log.info("Voucher {} used by user {} for order {}. Discount: {}đ", 
            code, user.getUsername(), order.getId(), discountAmount);
    }
    
    /**
     * Generate unique voucher code
     */
    private String generateVoucherCode(String prefix) {
        String randomPart = String.format("%04d", random.nextInt(10000));
        String code = prefix + randomPart;
        
        // Check if code exists, regenerate if needed
        while (voucherRepository.findByCode(code).isPresent()) {
            randomPart = String.format("%04d", random.nextInt(10000));
            code = prefix + randomPart;
        }
        
        return code;
    }
}
