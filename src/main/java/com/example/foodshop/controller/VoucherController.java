package com.example.foodshop.controller;

import com.example.foodshop.entity.User;
import com.example.foodshop.entity.Voucher;
import com.example.foodshop.repository.UserRepository;
import com.example.foodshop.repository.VoucherRepository;
import com.example.foodshop.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {
    
    private final VoucherService voucherService;
    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    
    /**
     * Validate voucher và tính discount amount
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateVoucher(
            @RequestParam String code,
            @RequestParam BigDecimal orderTotal,
            Authentication authentication
    ) {
        try {
            // Get user if authenticated
            User user = null;
            if (authentication != null) {
                String username = authentication.getName();
                user = userRepository.findByUsername(username).orElse(null);
            }
            
            // Find voucher
            Voucher voucher = voucherRepository.findValidVoucherByCode(code, LocalDateTime.now())
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ hoặc đã hết hạn"));
            
            // Check if user can use this voucher
            if (user != null && !voucher.canUse(user)) {
                return ResponseEntity.status(403).body("Bạn không thể sử dụng mã giảm giá này");
            }
            
            // Check min order value
            if (orderTotal.compareTo(voucher.getMinOrderValue()) < 0) {
                return ResponseEntity.status(400).body(
                    "Đơn hàng tối thiểu " + voucher.getMinOrderValue() + "đ để sử dụng mã này"
                );
            }
            
            // Calculate discount
            BigDecimal discountAmount = voucher.calculateDiscount(orderTotal);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("code", code);
            response.put("discountAmount", discountAmount.doubleValue());
            response.put("discountType", voucher.getDiscountType());
            response.put("description", voucher.getDescription());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
