package com.example.foodshop.payment.entity;

import com.example.foodshop.payment.saga.SagaStatus;
import com.example.foodshop.payment.saga.SagaStep;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_sagas", indexes = {
        @Index(name = "idx_saga_id", columnList = "saga_id"),
        @Index(name = "idx_payment_id", columnList = "payment_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
public class PaymentSaga {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "saga_id", unique = true, nullable = false, length = 50)
    private String sagaId;
    
    @Column(name = "payment_id")
    private Long paymentId;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SagaStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", length = 30)
    private SagaStep currentStep;
    
    @Column(name = "voucher_id")
    private Long voucherId;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "compensation_count")
    private Integer compensationCount = 0;
    
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    @Column(name = "max_retries")
    private Integer maxRetries = 3;
    
    @Column(name = "saga_data", columnDefinition = "TEXT")
    private String sagaData;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    // Constructors
    public PaymentSaga() {
    }
    
    public PaymentSaga(String sagaId, Long orderId, Long userId) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.userId = userId;
        this.status = SagaStatus.STARTED;
        this.currentStep = SagaStep.VALIDATE_ORDER;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSagaId() {
        return sagaId;
    }
    
    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }
    
    public Long getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public SagaStatus getStatus() {
        return status;
    }
    
    public void setStatus(SagaStatus status) {
        this.status = status;
    }
    
    public SagaStep getCurrentStep() {
        return currentStep;
    }
    
    public void setCurrentStep(SagaStep currentStep) {
        this.currentStep = currentStep;
    }
    
    public Long getVoucherId() {
        return voucherId;
    }
    
    public void setVoucherId(Long voucherId) {
        this.voucherId = voucherId;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Integer getCompensationCount() {
        return compensationCount;
    }
    
    public void setCompensationCount(Integer compensationCount) {
        this.compensationCount = compensationCount;
    }
    
    public Integer getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
    
    public Integer getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public String getSagaData() {
        return sagaData;
    }
    
    public void setSagaData(String sagaData) {
        this.sagaData = sagaData;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (status.isFinal() && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }
    
    // Business methods
    public void incrementRetry() {
        this.retryCount++;
    }
    
    public boolean canRetry() {
        return retryCount < maxRetries;
    }
    
    public void incrementCompensation() {
        this.compensationCount++;
    }
    
    public void moveToNextStep() {
        if (currentStep != null) {
            this.currentStep = currentStep.next();
        }
    }
    
    public void moveToPreviousStep() {
        if (currentStep != null) {
            this.currentStep = currentStep.previous();
        }
    }
}
