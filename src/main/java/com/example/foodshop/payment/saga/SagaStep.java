package com.example.foodshop.payment.saga;

public enum SagaStep {
    VALIDATE_ORDER(1, "Validate Order"),
    RESERVE_VOUCHER(2, "Reserve Voucher"),
    CREATE_PAYMENT(3, "Create Payment"),
    PROCESS_PAYMENT(4, "Process Payment"),
    CONFIRM_ORDER(5, "Confirm Order");
    
    private final int order;
    private final String description;
    
    SagaStep(int order, String description) {
        this.order = order;
        this.description = description;
    }
    
    public int getOrder() {
        return order;
    }
    
    public String getDescription() {
        return description;
    }
    
    public SagaStep next() {
        SagaStep[] steps = values();
        for (int i = 0; i < steps.length - 1; i++) {
            if (steps[i] == this) {
                return steps[i + 1];
            }
        }
        return null;
    }
    
    public SagaStep previous() {
        SagaStep[] steps = values();
        for (int i = 1; i < steps.length; i++) {
            if (steps[i] == this) {
                return steps[i - 1];
            }
        }
        return null;
    }
}
