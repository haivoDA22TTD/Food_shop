package com.example.foodshop.payment.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.foodshop.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableScheduling
public class AppConfig {
    
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
    
    @Autowired
    private PaymentService paymentService;
    
    // Auto-cancel expired payments every 5 minutes
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void cancelExpiredPayments() {
        try {
            log.info("Running scheduled task: Cancel expired payments");
            paymentService.cancelExpiredPayments();
        } catch (Exception e) {
            log.error("Error in scheduled task: {}", e.getMessage(), e);
        }
    }
}
