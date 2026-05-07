package com.example.foodshop.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RefundRequest {
    
    @NotBlank(message = "Refund reason is required")
    @Size(min = 10, max = 500, message = "Refund reason must be between 10 and 500 characters")
    private String reason;
    
    // Constructors
    public RefundRequest() {
    }
    
    public RefundRequest(String reason) {
        this.reason = reason;
    }
    
    // Getters and Setters
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}
