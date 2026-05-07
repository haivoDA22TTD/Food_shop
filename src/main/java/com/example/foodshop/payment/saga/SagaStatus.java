package com.example.foodshop.payment.saga;

public enum SagaStatus {
    STARTED("Saga started"),
    ORDER_VALIDATED("Order validated"),
    VOUCHER_RESERVED("Voucher reserved"),
    PAYMENT_CREATED("Payment created"),
    PAYMENT_PROCESSED("Payment processed"),
    ORDER_CONFIRMED("Order confirmed"),
    COMPLETED("Saga completed successfully"),
    
    // Compensation states
    COMPENSATING("Compensating transaction"),
    VOUCHER_RELEASED("Voucher released"),
    PAYMENT_CANCELLED("Payment cancelled"),
    ORDER_REVERTED("Order reverted"),
    FAILED("Saga failed"),
    COMPENSATED("Saga compensated");
    
    private final String description;
    
    SagaStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isCompensating() {
        return this == COMPENSATING || this == VOUCHER_RELEASED || 
               this == PAYMENT_CANCELLED || this == ORDER_REVERTED;
    }
    
    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == COMPENSATED;
    }
}
