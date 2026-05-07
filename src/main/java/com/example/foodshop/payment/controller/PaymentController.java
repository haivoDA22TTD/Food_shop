package com.example.foodshop.payment.controller;

import com.example.foodshop.payment.dto.request.CreatePaymentRequest;
import com.example.foodshop.payment.dto.response.PaymentResponse;
import com.example.foodshop.payment.entity.PaymentStatus;
import com.example.foodshop.payment.security.PaymentUserDetails;
import com.example.foodshop.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            Authentication authentication) {
        
        PaymentUserDetails userDetails = (PaymentUserDetails) authentication.getPrincipal();
        PaymentResponse response = paymentService.createPayment(userDetails.getUserId(), request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long id,
            Authentication authentication) {
        
        PaymentUserDetails userDetails = (PaymentUserDetails) authentication.getPrincipal();
        PaymentResponse response = paymentService.getPaymentById(id, userDetails.getUserId());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable Long orderId,
            Authentication authentication) {
        
        PaymentUserDetails userDetails = (PaymentUserDetails) authentication.getPrincipal();
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId, userDetails.getUserId());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getUserPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) PaymentStatus status,
            Authentication authentication) {
        
        PaymentUserDetails userDetails = (PaymentUserDetails) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentResponse> payments = paymentService.getUserPayments(
            userDetails.getUserId(), pageable, status);
        
        return ResponseEntity.ok(payments);
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable Long id,
            Authentication authentication) {
        
        PaymentUserDetails userDetails = (PaymentUserDetails) authentication.getPrincipal();
        PaymentResponse response = paymentService.cancelPayment(id, userDetails.getUserId());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/vnpay-callback")
    public ResponseEntity<PaymentResponse> handleVNPayCallback(@RequestParam Map<String, String> params) {
        PaymentResponse response = paymentService.handleVNPayCallback(params);
        return ResponseEntity.ok(response);
    }
}
