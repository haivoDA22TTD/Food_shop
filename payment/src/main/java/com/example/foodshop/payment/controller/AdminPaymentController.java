package com.example.foodshop.payment.controller;

import com.example.foodshop.payment.dto.request.CreateVoucherRequest;
import com.example.foodshop.payment.dto.request.RefundRequest;
import com.example.foodshop.payment.dto.response.PaymentResponse;
import com.example.foodshop.payment.dto.response.PaymentStatisticsResponse;
import com.example.foodshop.payment.dto.response.VoucherResponse;
import com.example.foodshop.payment.entity.PaymentStatus;
import com.example.foodshop.payment.entity.VoucherUsage;
import com.example.foodshop.payment.service.PaymentService;
import com.example.foodshop.payment.service.VoucherService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private VoucherService voucherService;
    
    // Payment Management
    
    @GetMapping("/payments")
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String keyword) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentResponse> payments = paymentService.getAllPayments(pageable, status, keyword);
        
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/payments/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        // Admin can view any payment - pass null as userId to skip ownership check
        PaymentResponse response = paymentService.getPaymentById(id, null);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/payments/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request) {
        
        PaymentResponse response = paymentService.refundPayment(id, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/payments/statistics")
    public ResponseEntity<PaymentStatisticsResponse> getPaymentStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        PaymentStatisticsResponse statistics = paymentService.getPaymentStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }
    
    // Voucher Management
    
    @PostMapping("/vouchers")
    public ResponseEntity<VoucherResponse> createVoucher(@Valid @RequestBody CreateVoucherRequest request) {
        VoucherResponse response = voucherService.createVoucher(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/vouchers")
    public ResponseEntity<Page<VoucherResponse>> getAllVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<VoucherResponse> vouchers = voucherService.getAllVouchers(pageable);
        
        return ResponseEntity.ok(vouchers);
    }
    
    @GetMapping("/vouchers/{id}")
    public ResponseEntity<VoucherResponse> getVoucherById(@PathVariable Long id) {
        VoucherResponse response = voucherService.getVoucherById(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/vouchers/{id}")
    public ResponseEntity<VoucherResponse> updateVoucher(
            @PathVariable Long id,
            @Valid @RequestBody CreateVoucherRequest request) {
        
        VoucherResponse response = voucherService.updateVoucher(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/vouchers/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/vouchers/{id}/usages")
    public ResponseEntity<Page<VoucherUsage>> getVoucherUsages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<VoucherUsage> usages = voucherService.getVoucherUsages(id, pageable);
        
        return ResponseEntity.ok(usages);
    }
}
