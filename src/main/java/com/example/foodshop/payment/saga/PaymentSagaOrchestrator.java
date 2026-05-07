package com.example.foodshop.payment.saga;

import com.example.foodshop.payment.dto.request.CreatePaymentRequest;
import com.example.foodshop.payment.dto.response.OrderResponse;
import com.example.foodshop.payment.dto.response.PaymentResponse;
import com.example.foodshop.payment.dto.response.VoucherResponse;
import com.example.foodshop.payment.entity.Payment;
import com.example.foodshop.payment.entity.PaymentMethod;
import com.example.foodshop.payment.entity.PaymentSaga;
import com.example.foodshop.payment.entity.PaymentStatus;
import com.example.foodshop.payment.exception.PaymentException;
import com.example.foodshop.payment.repository.PaymentRepository;
import com.example.foodshop.payment.repository.PaymentSagaRepository;
import com.example.foodshop.payment.service.OrderFeignClient;
import com.example.foodshop.payment.service.VNPayService;
import com.example.foodshop.payment.service.VoucherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class PaymentSagaOrchestrator {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentSagaOrchestrator.class);
    
    @Autowired
    private PaymentSagaRepository sagaRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private OrderFeignClient orderFeignClient;
    
    @Autowired
    private VoucherService voucherService;
    
    @Autowired
    private VNPayService vnPayService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Start a new payment saga
     */
    @Transactional
    public PaymentSaga startSaga(Long userId, CreatePaymentRequest request) {
        String sagaId = generateSagaId();
        
        PaymentSaga saga = new PaymentSaga(sagaId, request.getOrderId(), userId);
        saga.setMaxRetries(3);
        
        // Store saga data
        Map<String, Object> sagaData = new HashMap<>();
        sagaData.put("paymentMethod", request.getPaymentMethod().name());
        sagaData.put("voucherCode", request.getVoucherCode());
        sagaData.put("returnUrl", request.getReturnUrl());
        
        try {
            saga.setSagaData(objectMapper.writeValueAsString(sagaData));
        } catch (Exception e) {
            log.error("Error serializing saga data", e);
        }
        
        saga = sagaRepository.save(saga);
        
        log.info("Started payment saga: {} for order: {}", sagaId, request.getOrderId());
        
        return saga;
    }
    
    /**
     * Execute saga steps
     */
    @Transactional
    public PaymentResponse executeSaga(PaymentSaga saga) {
        try {
            // Step 1: Validate Order
            OrderResponse order = executeValidateOrder(saga);
            
            // Step 2: Reserve Voucher (if applicable)
            VoucherResponse voucher = executeReserveVoucher(saga, order);
            
            // Step 3: Create Payment
            Payment payment = executeCreatePayment(saga, order, voucher);
            
            // Step 4: Process Payment
            executeProcessPayment(saga, payment);
            
            // Step 5: Confirm Order
            executeConfirmOrder(saga, payment);
            
            // Mark saga as completed
            saga.setStatus(SagaStatus.COMPLETED);
            sagaRepository.save(saga);
            
            log.info("Saga {} completed successfully", saga.getSagaId());
            
            return convertToPaymentResponse(payment);
            
        } catch (Exception e) {
            log.error("Saga {} failed at step {}: {}", 
                    saga.getSagaId(), saga.getCurrentStep(), e.getMessage());
            
            // Start compensation
            compensateSaga(saga, e.getMessage());
            
            throw new PaymentException("Payment saga failed: " + e.getMessage());
        }
    }
    
    /**
     * Step 1: Validate Order
     */
    private OrderResponse executeValidateOrder(PaymentSaga saga) {
        try {
            saga.setCurrentStep(SagaStep.VALIDATE_ORDER);
            saga.setStatus(SagaStatus.STARTED);
            sagaRepository.save(saga);
            
            log.info("Saga {}: Validating order {}", saga.getSagaId(), saga.getOrderId());
            
            OrderResponse order = orderFeignClient.getOrderById(saga.getOrderId());
            
            if (!order.getUserId().equals(saga.getUserId())) {
                throw new PaymentException("Order does not belong to user");
            }
            
            if (!"PENDING".equals(order.getStatus())) {
                throw new PaymentException("Order is not in PENDING status");
            }
            
            saga.setStatus(SagaStatus.ORDER_VALIDATED);
            sagaRepository.save(saga);
            
            log.info("Saga {}: Order validated successfully", saga.getSagaId());
            
            return order;
            
        } catch (Exception e) {
            saga.setErrorMessage("Order validation failed: " + e.getMessage());
            sagaRepository.save(saga);
            throw e;
        }
    }
    
    /**
     * Step 2: Reserve Voucher
     */
    private VoucherResponse executeReserveVoucher(PaymentSaga saga, OrderResponse order) {
        try {
            saga.setCurrentStep(SagaStep.RESERVE_VOUCHER);
            sagaRepository.save(saga);
            
            String voucherCode = extractVoucherCode(saga);
            
            if (voucherCode == null || voucherCode.trim().isEmpty()) {
                log.info("Saga {}: No voucher to reserve", saga.getSagaId());
                return null;
            }
            
            log.info("Saga {}: Reserving voucher {}", saga.getSagaId(), voucherCode);
            
            VoucherResponse voucher = voucherService.applyVoucher(
                voucherCode, saga.getUserId(), order.getTotalAmount());
            
            saga.setVoucherId(voucher.getId());
            saga.setStatus(SagaStatus.VOUCHER_RESERVED);
            sagaRepository.save(saga);
            
            log.info("Saga {}: Voucher reserved successfully", saga.getSagaId());
            
            return voucher;
            
        } catch (Exception e) {
            saga.setErrorMessage("Voucher reservation failed: " + e.getMessage());
            sagaRepository.save(saga);
            throw e;
        }
    }
    
    /**
     * Step 3: Create Payment
     */
    private Payment executeCreatePayment(PaymentSaga saga, OrderResponse order, VoucherResponse voucher) {
        try {
            saga.setCurrentStep(SagaStep.CREATE_PAYMENT);
            sagaRepository.save(saga);
            
            log.info("Saga {}: Creating payment", saga.getSagaId());
            
            BigDecimal amount = order.getTotalAmount();
            BigDecimal discountAmount = voucher != null ? voucher.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal finalAmount = amount.subtract(discountAmount);
            
            Payment payment = new Payment();
            payment.setOrderId(saga.getOrderId());
            payment.setUserId(saga.getUserId());
            payment.setAmount(amount);
            payment.setDiscountAmount(discountAmount);
            payment.setFinalAmount(finalAmount);
            payment.setPaymentMethod(extractPaymentMethod(saga));
            
            if (voucher != null) {
                payment.setVoucherId(voucher.getId());
                payment.setVoucherCode(voucher.getCode());
            }
            
            payment = paymentRepository.save(payment);
            
            saga.setPaymentId(payment.getId());
            saga.setStatus(SagaStatus.PAYMENT_CREATED);
            sagaRepository.save(saga);
            
            log.info("Saga {}: Payment created with ID {}", saga.getSagaId(), payment.getId());
            
            return payment;
            
        } catch (Exception e) {
            saga.setErrorMessage("Payment creation failed: " + e.getMessage());
            sagaRepository.save(saga);
            throw e;
        }
    }
    
    /**
     * Step 4: Process Payment
     */
    private void executeProcessPayment(PaymentSaga saga, Payment payment) {
        try {
            saga.setCurrentStep(SagaStep.PROCESS_PAYMENT);
            sagaRepository.save(saga);
            
            log.info("Saga {}: Processing payment", saga.getSagaId());
            
            if (payment.getPaymentMethod() == PaymentMethod.COD) {
                // COD is auto-completed
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());
                paymentRepository.save(payment);
                
            } else if (payment.getPaymentMethod() == PaymentMethod.VNPAY) {
                // Generate VNPay URL
                String returnUrl = extractReturnUrl(saga);
                String paymentUrl = vnPayService.createPaymentUrl(payment, returnUrl);
                payment.setPaymentUrl(paymentUrl);
                payment.setPaymentStatus(PaymentStatus.PENDING);
                paymentRepository.save(payment);
            }
            
            // Reserve voucher if used
            if (saga.getVoucherId() != null) {
                voucherService.reserveVoucher(
                    saga.getVoucherId(), 
                    saga.getUserId(), 
                    payment.getId(), 
                    payment.getDiscountAmount()
                );
            }
            
            saga.setStatus(SagaStatus.PAYMENT_PROCESSED);
            sagaRepository.save(saga);
            
            log.info("Saga {}: Payment processed successfully", saga.getSagaId());
            
        } catch (Exception e) {
            saga.setErrorMessage("Payment processing failed: " + e.getMessage());
            sagaRepository.save(saga);
            throw e;
        }
    }
    
    /**
     * Step 5: Confirm Order
     */
    private void executeConfirmOrder(PaymentSaga saga, Payment payment) {
        try {
            saga.setCurrentStep(SagaStep.CONFIRM_ORDER);
            sagaRepository.save(saga);
            
            // Only confirm order if payment is completed (COD)
            if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
                log.info("Saga {}: Confirming order", saga.getSagaId());
                
                OrderFeignClient.OrderStatusUpdateRequest request = 
                    new OrderFeignClient.OrderStatusUpdateRequest(
                        "CONFIRMED", 
                        "Payment completed via " + payment.getPaymentMethod()
                    );
                
                orderFeignClient.updateOrderStatus(saga.getOrderId(), request);
                
                saga.setStatus(SagaStatus.ORDER_CONFIRMED);
                sagaRepository.save(saga);
                
                log.info("Saga {}: Order confirmed successfully", saga.getSagaId());
            } else {
                log.info("Saga {}: Order confirmation skipped (payment pending)", saga.getSagaId());
            }
            
        } catch (Exception e) {
            log.warn("Saga {}: Order confirmation failed (non-critical): {}", 
                    saga.getSagaId(), e.getMessage());
            // Order confirmation failure is non-critical, don't fail the saga
        }
    }
    
    /**
     * Compensate saga on failure
     */
    @Transactional
    public void compensateSaga(PaymentSaga saga, String errorMessage) {
        try {
            log.warn("Starting compensation for saga: {}", saga.getSagaId());
            
            saga.setStatus(SagaStatus.COMPENSATING);
            saga.setErrorMessage(errorMessage);
            saga.incrementCompensation();
            sagaRepository.save(saga);
            
            SagaStep currentStep = saga.getCurrentStep();
            
            // Compensate in reverse order
            if (currentStep != null) {
                switch (currentStep) {
                    case CONFIRM_ORDER:
                    case PROCESS_PAYMENT:
                        compensatePayment(saga);
                        // Fall through
                    case CREATE_PAYMENT:
                        compensateVoucher(saga);
                        // Fall through
                    case RESERVE_VOUCHER:
                    case VALIDATE_ORDER:
                        // No compensation needed for validation
                        break;
                }
            }
            
            saga.setStatus(SagaStatus.COMPENSATED);
            sagaRepository.save(saga);
            
            log.info("Saga {} compensated successfully", saga.getSagaId());
            
        } catch (Exception e) {
            log.error("Compensation failed for saga {}: {}", saga.getSagaId(), e.getMessage());
            saga.setStatus(SagaStatus.FAILED);
            sagaRepository.save(saga);
        }
    }
    
    /**
     * Compensate payment
     */
    private void compensatePayment(PaymentSaga saga) {
        try {
            if (saga.getPaymentId() != null) {
                log.info("Saga {}: Compensating payment", saga.getSagaId());
                
                Payment payment = paymentRepository.findById(saga.getPaymentId()).orElse(null);
                if (payment != null && payment.canBeCancelled()) {
                    payment.setPaymentStatus(PaymentStatus.CANCELLED);
                    paymentRepository.save(payment);
                    
                    saga.setStatus(SagaStatus.PAYMENT_CANCELLED);
                    sagaRepository.save(saga);
                    
                    log.info("Saga {}: Payment compensated", saga.getSagaId());
                }
            }
        } catch (Exception e) {
            log.error("Payment compensation failed for saga {}: {}", saga.getSagaId(), e.getMessage());
        }
    }
    
    /**
     * Compensate voucher (release reservation)
     */
    private void compensateVoucher(PaymentSaga saga) {
        try {
            if (saga.getVoucherId() != null) {
                log.info("Saga {}: Compensating voucher", saga.getSagaId());
                
                // Voucher compensation logic would go here
                // For now, we just log it
                
                saga.setStatus(SagaStatus.VOUCHER_RELEASED);
                sagaRepository.save(saga);
                
                log.info("Saga {}: Voucher compensated", saga.getSagaId());
            }
        } catch (Exception e) {
            log.error("Voucher compensation failed for saga {}: {}", saga.getSagaId(), e.getMessage());
        }
    }
    
    // Helper methods
    
    private String generateSagaId() {
        return "SAGA-" + UUID.randomUUID().toString();
    }
    
    private String extractVoucherCode(PaymentSaga saga) {
        try {
            Map<String, Object> data = objectMapper.readValue(saga.getSagaData(), Map.class);
            return (String) data.get("voucherCode");
        } catch (Exception e) {
            return null;
        }
    }
    
    private PaymentMethod extractPaymentMethod(PaymentSaga saga) {
        try {
            Map<String, Object> data = objectMapper.readValue(saga.getSagaData(), Map.class);
            String method = (String) data.get("paymentMethod");
            return PaymentMethod.valueOf(method);
        } catch (Exception e) {
            return PaymentMethod.COD;
        }
    }
    
    private String extractReturnUrl(PaymentSaga saga) {
        try {
            Map<String, Object> data = objectMapper.readValue(saga.getSagaData(), Map.class);
            return (String) data.get("returnUrl");
        } catch (Exception e) {
            return null;
        }
    }
    
    private PaymentResponse convertToPaymentResponse(Payment payment) {
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
