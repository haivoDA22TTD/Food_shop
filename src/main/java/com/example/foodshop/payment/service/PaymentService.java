package com.example.foodshop.payment.service;

import com.example.foodshop.payment.dto.request.CreatePaymentRequest;
import com.example.foodshop.payment.dto.request.RefundRequest;
import com.example.foodshop.payment.dto.response.OrderResponse;
import com.example.foodshop.payment.dto.response.PaymentResponse;
import com.example.foodshop.payment.dto.response.PaymentStatisticsResponse;
import com.example.foodshop.payment.dto.response.VoucherResponse;
import com.example.foodshop.payment.entity.Payment;
import com.example.foodshop.payment.entity.PaymentMethod;
import com.example.foodshop.payment.entity.PaymentStatus;
import com.example.foodshop.payment.exception.PaymentException;
import com.example.foodshop.payment.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class PaymentService {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final String PAYMENT_CACHE_PREFIX = "payment:";
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private VoucherService voucherService;
    
    @Autowired
    private VNPayService vnPayService;
    
    @Autowired
    private OrderFeignClient orderFeignClient;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private com.example.foodshop.payment.saga.PaymentSagaOrchestrator sagaOrchestrator;
    
    @Autowired
    private com.example.foodshop.payment.repository.PaymentSagaRepository sagaRepository;
    
    @Value("${app.payment.cache-ttl:900}")
    private long paymentCacheTtl;
    
    @Value("${app.payment.saga-enabled:true}")
    private boolean sagaEnabled;
    
    public PaymentResponse createPayment(Long userId, CreatePaymentRequest request) {
        if (sagaEnabled) {
            return createPaymentWithSaga(userId, request);
        } else {
            return createPaymentLegacy(userId, request);
        }
    }
    
    /**
     * Create payment using Saga pattern
     */
    @Transactional
    public PaymentResponse createPaymentWithSaga(Long userId, CreatePaymentRequest request) {
        try {
            log.info("Creating payment with Saga pattern for user {} and order {}", 
                    userId, request.getOrderId());
            
            // Start saga
            com.example.foodshop.payment.entity.PaymentSaga saga = 
                sagaOrchestrator.startSaga(userId, request);
            
            // Execute saga
            PaymentResponse response = sagaOrchestrator.executeSaga(saga);
            
            log.info("Payment created successfully via Saga: {}", response.getPaymentNumber());
            
            return response;
            
        } catch (Exception e) {
            log.error("Saga payment creation failed for user {}: {}", userId, e.getMessage(), e);
            throw new PaymentException("Unable to create payment: " + e.getMessage());
        }
    }
    
    /**
     * Legacy payment creation (without Saga)
     */
    @Transactional
    public PaymentResponse createPaymentLegacy(Long userId, CreatePaymentRequest request) {
        try {
            // 1. Validate order exists and belongs to user
            OrderResponse order = orderFeignClient.getOrderById(request.getOrderId());
            if (!order.getUserId().equals(userId)) {
                throw new PaymentException("Order not found or access denied");
            }
            
            // 2. Check if payment already exists for this order
            Optional<Payment> existingPayment = paymentRepository
                .findByOrderIdAndPaymentStatusIn(request.getOrderId(), 
                    List.of(PaymentStatus.PENDING, PaymentStatus.PROCESSING, PaymentStatus.COMPLETED));
            
            if (existingPayment.isPresent()) {
                throw new PaymentException("Payment already exists for this order");
            }
            
            // 3. Calculate amounts
            BigDecimal amount = order.getTotalAmount();
            BigDecimal discountAmount = BigDecimal.ZERO;
            Long voucherId = null;
            String voucherCode = null;
            
            // 4. Apply voucher if provided
            if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
                VoucherResponse voucher = voucherService.applyVoucher(
                    request.getVoucherCode(), userId, amount);
                discountAmount = voucher.getDiscountAmount();
                voucherId = voucher.getId();
                voucherCode = voucher.getCode();
            }
            
            BigDecimal finalAmount = amount.subtract(discountAmount);
            
            // 5. Create payment record
            Payment payment = new Payment();
            payment.setOrderId(request.getOrderId());
            payment.setUserId(userId);
            payment.setAmount(amount);
            payment.setDiscountAmount(discountAmount);
            payment.setFinalAmount(finalAmount);
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setVoucherId(voucherId);
            payment.setVoucherCode(voucherCode);
            
            // 6. Handle payment method
            if (request.getPaymentMethod() == PaymentMethod.COD) {
                // COD is auto-completed
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());
            } else if (request.getPaymentMethod() == PaymentMethod.VNPAY) {
                // Generate VNPay payment URL
                payment.setPaymentStatus(PaymentStatus.PENDING);
                payment = paymentRepository.save(payment);
                
                String paymentUrl = vnPayService.createPaymentUrl(payment, request.getReturnUrl());
                payment.setPaymentUrl(paymentUrl);
            } else {
                // Other online payment methods
                payment.setPaymentStatus(PaymentStatus.PENDING);
            }
            
            payment = paymentRepository.save(payment);
            
            // 7. Reserve voucher if used
            if (voucherId != null) {
                voucherService.reserveVoucher(voucherId, userId, payment.getId(), discountAmount);
            }
            
            // 8. Cache payment info
            cachePayment(payment);
            
            // 9. Update order status if payment completed
            if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
                updateOrderStatus(payment.getOrderId(), "CONFIRMED", "Payment completed");
            }
            
            log.info("Created payment {} for order {} with amount {}", 
                    payment.getPaymentNumber(), payment.getOrderId(), finalAmount);
            
            return convertToResponse(payment);
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating payment for user {}: {}", userId, e.getMessage(), e);
            throw new PaymentException("Unable to create payment: " + e.getMessage());
        }
    }
    
    public PaymentResponse handleVNPayCallback(Map<String, String> params) {
        try {
            String paymentNumber = params.get("vnp_TxnRef");
            
            Payment payment = paymentRepository.findByPaymentNumber(paymentNumber)
                .orElseThrow(() -> new PaymentException("Payment not found"));
            
            // Validate callback signature
            boolean isValid = vnPayService.validateCallback(params);
            
            if (isValid && "00".equals(params.get("vnp_ResponseCode"))) {
                // Payment successful
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());
                payment.setTransactionId(params.get("vnp_TransactionNo"));
                
                // Update order status
                updateOrderStatus(payment.getOrderId(), "CONFIRMED", "Payment completed via VNPay");
                
                // Update saga if exists
                updateSagaOnSuccess(payment);
                
                log.info("VNPay payment completed: {}", paymentNumber);
            } else {
                // Payment failed
                payment.setPaymentStatus(PaymentStatus.FAILED);
                
                // Compensate saga if exists
                compensateSagaOnFailure(payment, "VNPay payment failed: " + params.get("vnp_ResponseCode"));
                
                log.warn("VNPay payment failed: {} - Response code: {}", 
                        paymentNumber, params.get("vnp_ResponseCode"));
            }
            
            // Store gateway response
            ObjectMapper mapper = new ObjectMapper();
            payment.setPaymentGatewayResponse(mapper.writeValueAsString(params));
            
            payment = paymentRepository.save(payment);
            
            // Update cache
            cachePayment(payment);
            
            return convertToResponse(payment);
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error handling VNPay callback: {}", e.getMessage(), e);
            throw new PaymentException("Error processing payment callback");
        }
    }
    
    private void updateSagaOnSuccess(Payment payment) {
        try {
            sagaRepository.findByPaymentId(payment.getId()).ifPresent(saga -> {
                saga.setStatus(com.example.foodshop.payment.saga.SagaStatus.COMPLETED);
                sagaRepository.save(saga);
                log.info("Saga {} marked as completed", saga.getSagaId());
            });
        } catch (Exception e) {
            log.warn("Failed to update saga on payment success: {}", e.getMessage());
        }
    }
    
    private void compensateSagaOnFailure(Payment payment, String errorMessage) {
        try {
            sagaRepository.findByPaymentId(payment.getId()).ifPresent(saga -> {
                sagaOrchestrator.compensateSaga(saga, errorMessage);
                log.info("Saga {} compensated due to payment failure", saga.getSagaId());
            });
        } catch (Exception e) {
            log.warn("Failed to compensate saga on payment failure: {}", e.getMessage());
        }
    }
    
    public PaymentResponse getPaymentById(Long paymentId, Long userId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentException("Payment not found"));
        
        if (!payment.getUserId().equals(userId)) {
            throw new PaymentException("Access denied");
        }
        
        return convertToResponse(payment);
    }
    
    public PaymentResponse getPaymentByOrderId(Long orderId, Long userId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new PaymentException("Payment not found for this order"));
        
        if (!payment.getUserId().equals(userId)) {
            throw new PaymentException("Access denied");
        }
        
        return convertToResponse(payment);
    }
    
    public Page<PaymentResponse> getUserPayments(Long userId, Pageable pageable, PaymentStatus status) {
        Page<Payment> payments;
        
        if (status != null) {
            payments = paymentRepository.findByUserIdAndPaymentStatusOrderByCreatedAtDesc(
                userId, status, pageable);
        } else {
            payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        
        return payments.map(this::convertToResponse);
    }
    
    public PaymentResponse cancelPayment(Long paymentId, Long userId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentException("Payment not found"));
        
        if (!payment.getUserId().equals(userId)) {
            throw new PaymentException("Access denied");
        }
        
        if (!payment.canBeCancelled()) {
            throw new PaymentException("Payment cannot be cancelled in current status: " + 
                payment.getPaymentStatus());
        }
        
        payment.setPaymentStatus(PaymentStatus.CANCELLED);
        payment = paymentRepository.save(payment);
        
        // Update cache
        cachePayment(payment);
        
        log.info("Cancelled payment {} by user {}", payment.getPaymentNumber(), userId);
        
        return convertToResponse(payment);
    }
    
    // Admin methods
    
    public Page<PaymentResponse> getAllPayments(Pageable pageable, PaymentStatus status, String keyword) {
        Page<Payment> payments;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            payments = paymentRepository.searchPayments(keyword, pageable);
        } else if (status != null) {
            payments = paymentRepository.findByPaymentStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            payments = paymentRepository.findAll(pageable);
        }
        
        return payments.map(this::convertToResponse);
    }
    
    public PaymentResponse refundPayment(Long paymentId, RefundRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentException("Payment not found"));
        
        if (!payment.canBeRefunded()) {
            throw new PaymentException("Payment cannot be refunded in current status: " + 
                payment.getPaymentStatus());
        }
        
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        payment.setRefundReason(request.getReason());
        payment.setRefundedAt(LocalDateTime.now());
        
        payment = paymentRepository.save(payment);
        
        // Update cache
        cachePayment(payment);
        
        // Update order status
        updateOrderStatus(payment.getOrderId(), "CANCELLED", "Payment refunded: " + request.getReason());
        
        log.info("Refunded payment {} - Reason: {}", payment.getPaymentNumber(), request.getReason());
        
        return convertToResponse(payment);
    }
    
    public PaymentStatisticsResponse getPaymentStatistics(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Object[]> stats = paymentRepository.getPaymentStatistics(startDateTime, endDateTime);
        
        PaymentStatisticsResponse response = new PaymentStatisticsResponse(startDate, endDate);
        Map<String, Long> paymentsByStatus = new HashMap<>();
        Map<String, BigDecimal> revenueByStatus = new HashMap<>();
        
        long totalPayments = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        
        for (Object[] stat : stats) {
            PaymentStatus status = (PaymentStatus) stat[0];
            Long count = (Long) stat[1];
            BigDecimal revenue = (BigDecimal) stat[2];
            
            paymentsByStatus.put(status.name(), count);
            revenueByStatus.put(status.name(), revenue != null ? revenue : BigDecimal.ZERO);
            
            totalPayments += count;
            if (revenue != null && status == PaymentStatus.COMPLETED) {
                totalRevenue = totalRevenue.add(revenue);
            }
        }
        
        response.setTotalPayments(totalPayments);
        response.setTotalRevenue(totalRevenue);
        response.setPaymentsByStatus(paymentsByStatus);
        response.setRevenueByStatus(revenueByStatus);
        response.calculateAveragePaymentAmount();
        
        return response;
    }
    
    // Scheduled task to auto-cancel expired payments
    public void cancelExpiredPayments() {
        List<Payment> expiredPayments = paymentRepository
            .findByPaymentStatusAndExpiredAtBefore(PaymentStatus.PENDING, LocalDateTime.now());
        
        for (Payment payment : expiredPayments) {
            payment.setPaymentStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
            
            log.info("Auto-cancelled expired payment: {}", payment.getPaymentNumber());
        }
    }
    
    // Helper methods
    
    private void updateOrderStatus(Long orderId, String status, String reason) {
        try {
            OrderFeignClient.OrderStatusUpdateRequest request = 
                new OrderFeignClient.OrderStatusUpdateRequest(status, reason);
            orderFeignClient.updateOrderStatus(orderId, request);
        } catch (Exception e) {
            log.error("Error updating order status for order {}: {}", orderId, e.getMessage());
        }
    }
    
    private void cachePayment(Payment payment) {
        String cacheKey = PAYMENT_CACHE_PREFIX + payment.getId();
        redisTemplate.opsForValue().set(cacheKey, payment, paymentCacheTtl, TimeUnit.SECONDS);
    }
    
    private PaymentResponse convertToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setPaymentNumber(payment.getPaymentNumber());
        response.setOrderId(payment.getOrderId());
        response.setUserId(payment.getUserId());
        response.setAmount(payment.getAmount());
        response.setDiscountAmount(payment.getDiscountAmount());
        response.setFinalAmount(payment.getFinalAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setPaymentStatus(payment.getPaymentStatus());
        response.setVoucherCode(payment.getVoucherCode());
        response.setTransactionId(payment.getTransactionId());
        response.setPaymentUrl(payment.getPaymentUrl());
        response.setPaidAt(payment.getPaidAt());
        response.setExpiredAt(payment.getExpiredAt());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        return response;
    }
}
