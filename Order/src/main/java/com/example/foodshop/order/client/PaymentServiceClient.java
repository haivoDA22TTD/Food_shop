package com.example.foodshop.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "payment-service", path = "/api/payments")
public interface PaymentServiceClient {
    
    @PostMapping
    ResponseEntity<PaymentResponse> createPayment(
            @RequestBody CreatePaymentRequest request,
            @RequestHeader("Authorization") String authorization
    );
}