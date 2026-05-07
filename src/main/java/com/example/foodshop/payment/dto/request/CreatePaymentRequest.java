package com.example.foodshop.payment.dto.request;

import com.example.foodshop.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreatePaymentRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    @Size(max = 50, message = "Voucher code cannot exceed 50 characters")
    private String voucherCode;
    
    private String returnUrl;
    
    // Constructors
    public CreatePaymentRequest() {
    }
    
    public CreatePaymentRequest(Long orderId, PaymentMethod paymentMethod, String voucherCode, String returnUrl) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.voucherCode = voucherCode;
        this.returnUrl = returnUrl;
    }
    
    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getVoucherCode() {
        return voucherCode;
    }
    
    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }
    
    public String getReturnUrl() {
        return returnUrl;
    }
    
    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
}
