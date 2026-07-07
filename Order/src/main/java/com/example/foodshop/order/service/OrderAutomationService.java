package com.example.foodshop.order.service;

import com.example.foodshop.order.config.OrderAutomationConfig;
import com.example.foodshop.order.entity.Order;
import com.example.foodshop.order.entity.OrderStatus;
import com.example.foodshop.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class OrderAutomationService {
    
    private static final Logger log = LoggerFactory.getLogger(OrderAutomationService.class);
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderAutomationConfig config;
    
    /**
     * Check if order should be auto-confirmed
     */
    public boolean shouldAutoConfirm(Order order) {
        if (!config.isAutoConfirmEnabled()) {
            log.debug("Auto-confirm is disabled");
            return false;
        }
        
        // Check business hours
        if (config.isAutoConfirmOnlyInBusinessHours() && !isWithinBusinessHours()) {
            log.debug("Outside business hours, skipping auto-confirm");
            return false;
        }
        
        // Check order amount
        BigDecimal amount = order.getTotalAmount();
        if (amount.compareTo(BigDecimal.valueOf(config.getMinOrderAmount())) < 0) {
            log.debug("Order amount {} is below minimum {}", amount, config.getMinOrderAmount());
            return false;
        }
        
        if (amount.compareTo(BigDecimal.valueOf(config.getMaxOrderAmount())) > 0) {
            log.debug("Order amount {} exceeds maximum {}, requires manual confirmation", 
                     amount, config.getMaxOrderAmount());
            return false;
        }
        
        // Check if order has valid items
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            log.warn("Order {} has no items", order.getOrderNumber());
            return false;
        }
        
        // Check if order has valid contact info
        if (order.getPhoneNumber() == null || order.getPhoneNumber().trim().isEmpty()) {
            log.warn("Order {} has no phone number", order.getOrderNumber());
            return false;
        }
        
        if (order.getShippingAddress() == null || order.getShippingAddress().trim().isEmpty()) {
            log.warn("Order {} has no shipping address", order.getOrderNumber());
            return false;
        }
        
        // FIX: Không auto-confirm đơn hàng thanh toán online (ZALOPAY, VNPAY, MOMO, BANK_TRANSFER)
        // Các đơn này phải chờ callback từ cổng thanh toán xác nhận tiền đã vào.
        // Chỉ COD mới được auto-confirm ngay vì không cần thanh toán trước.
        String paymentMethod = order.getPaymentMethod();
        if (paymentMethod != null && !paymentMethod.equalsIgnoreCase("COD")) {
            log.info("Order {} uses online payment method '{}', skipping auto-confirm — waiting for payment callback",
                    order.getOrderNumber(), paymentMethod);
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if current time is within business hours
     */
    private boolean isWithinBusinessHours() {
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.of(config.getBusinessHoursStart(), 0);
        LocalTime end = LocalTime.of(config.getBusinessHoursEnd(), 0);
        
        return !now.isBefore(start) && !now.isAfter(end);
    }
    
    /**
     * Auto-confirm order if conditions are met
     */
    @Transactional
    public void autoConfirmOrder(Order order) {
        if (shouldAutoConfirm(order)) {
            order.setStatus(OrderStatus.CONFIRMED);
            order.setAutoConfirmed(true);
            orderRepository.save(order);
            
            log.info("Order {} auto-confirmed successfully", order.getOrderNumber());
            
            // TODO: Send notification to kitchen
            // TODO: Send SMS/Email to customer
        } else {
            log.info("Order {} requires manual confirmation", order.getOrderNumber());
        }
    }
    
    /**
     * Scheduled task to auto-cancel expired pending orders
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void autoCancelExpiredOrders() {
        if (!config.isAutoCancelEnabled()) {
            return;
        }
        
        LocalDateTime expireTime = LocalDateTime.now()
            .minusMinutes(config.getAutoCancelMinutes());
        
        List<Order> expiredOrders = orderRepository
            .findByStatusAndCreatedAtBefore(OrderStatus.PENDING, expireTime);
        
        if (expiredOrders.isEmpty()) {
            log.debug("No expired orders found");
            return;
        }
        
        log.info("Found {} expired orders to cancel", expiredOrders.size());
        
        for (Order order : expiredOrders) {
            try {
                order.setStatus(OrderStatus.CANCELLED);
                order.setCancellationReason(
                    String.format("Tự động hủy do không được xác nhận trong %d phút", 
                                config.getAutoCancelMinutes())
                );
                orderRepository.save(order);
                
                log.info("Order {} auto-cancelled due to expiration", order.getOrderNumber());
                
                // TODO: Send notification to customer
                // TODO: Restore product stock if needed
                
            } catch (Exception e) {
                log.error("Error auto-cancelling order {}: {}", 
                         order.getOrderNumber(), e.getMessage(), e);
            }
        }
        
        log.info("Auto-cancelled {} expired orders", expiredOrders.size());
    }
    
    /**
     * Scheduled task to log automation status
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void logAutomationStatus() {
        log.info("Order Automation Status:");
        log.info("  - Auto-confirm enabled: {}", config.isAutoConfirmEnabled());
        log.info("  - Auto-cancel enabled: {}", config.isAutoCancelEnabled());
        log.info("  - Auto-cancel after: {} minutes", config.getAutoCancelMinutes());
        log.info("  - Business hours: {}:00 - {}:00", 
                config.getBusinessHoursStart(), config.getBusinessHoursEnd());
        log.info("  - Order amount range: {} - {} VND", 
                config.getMinOrderAmount(), config.getMaxOrderAmount());
        
        // Count pending orders
        long pendingCount = orderRepository.countByStatus(OrderStatus.PENDING);
        log.info("  - Current pending orders: {}", pendingCount);
    }
}
