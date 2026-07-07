package com.example.foodshop.payment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ApplyVoucherRequest {
    
    @NotBlank(message = "Voucher code is required")
    private String voucherCode;
    
    @NotNull(message = "Order amount is required")
    @DecimalMin(value = "0.01", message = "Order amount must be greater than 0")
    private BigDecimal orderAmount;
    
    // Constructors
    public ApplyVoucherRequest() {
    }
    
    public ApplyVoucherRequest(String voucherCode, BigDecimal orderAmount) {
        this.voucherCode = voucherCode;
        this.orderAmount = orderAmount;
    }
    
    // Getters and Setters
    public String getVoucherCode() {
        return voucherCode;
    }
    
    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }
    
    public BigDecimal getOrderAmount() {
        return orderAmount;
    }
    
    public void setOrderAmount(BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
    }
}
