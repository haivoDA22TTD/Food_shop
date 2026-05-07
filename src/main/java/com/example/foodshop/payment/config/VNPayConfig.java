package com.example.foodshop.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {
    
    @Value("${vnpay.tmn-code}")
    private String tmnCode;
    
    @Value("${vnpay.hash-secret}")
    private String hashSecret;
    
    @Value("${vnpay.url}")
    private String vnpayUrl;
    
    @Value("${vnpay.return-url}")
    private String returnUrl;
    
    public String getTmnCode() {
        return tmnCode;
    }
    
    public String getHashSecret() {
        return hashSecret;
    }
    
    public String getVnpayUrl() {
        return vnpayUrl;
    }
    
    public String getReturnUrl() {
        return returnUrl;
    }
}
