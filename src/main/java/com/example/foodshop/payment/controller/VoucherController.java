package com.example.foodshop.payment.controller;

import com.example.foodshop.payment.dto.request.ApplyVoucherRequest;
import com.example.foodshop.payment.dto.response.VoucherResponse;
import com.example.foodshop.payment.entity.VoucherUsage;
import com.example.foodshop.payment.security.PaymentUserDetails;
import com.example.foodshop.payment.service.VoucherService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {
    
    @Autowired
    private VoucherService voucherService;
    
    @GetMapping
    public ResponseEntity<List<VoucherResponse>> getAvailableVouchers() {
        List<VoucherResponse> vouchers = voucherService.getAvailableVouchers();
        return ResponseEntity.ok(vouchers);
    }
    
    @PostMapping("/validate")
    public ResponseEntity<VoucherResponse> validateVoucher(
            @Valid @RequestBody ApplyVoucherRequest request,
            Authentication authentication) {
        
        PaymentUserDetails userDetails = (PaymentUserDetails) authentication.getPrincipal();
        VoucherResponse response = voucherService.applyVoucher(
            request.getVoucherCode(), 
            userDetails.getUserId(), 
            request.getOrderAmount()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/my-usages")
    public ResponseEntity<List<VoucherUsage>> getMyVoucherUsages(Authentication authentication) {
        PaymentUserDetails userDetails = (PaymentUserDetails) authentication.getPrincipal();
        List<VoucherUsage> usages = voucherService.getUserVoucherUsages(userDetails.getUserId());
        
        return ResponseEntity.ok(usages);
    }
}
