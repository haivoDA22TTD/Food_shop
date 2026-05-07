package com.example.foodshop.payment.entity;

public enum PaymentMethod {
    COD("Thanh toán khi nhận hàng"),
    VNPAY("VNPay"),
    MOMO("MoMo"),
    BANK_TRANSFER("Chuyển khoản ngân hàng");
    
    private final String displayName;
    
    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean requiresOnlinePayment() {
        return this != COD;
    }
}
