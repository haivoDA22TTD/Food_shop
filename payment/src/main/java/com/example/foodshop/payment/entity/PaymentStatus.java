package com.example.foodshop.payment.entity;

public enum PaymentStatus {
    PENDING("Chờ thanh toán"),
    PROCESSING("Đang xử lý"),
    COMPLETED("Hoàn thành"),
    FAILED("Thất bại"),
    CANCELLED("Đã hủy"),
    REFUNDED("Đã hoàn tiền"),
    EXPIRED("Hết hạn");
    
    private final String displayName;
    
    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean canTransitionTo(PaymentStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == PROCESSING || newStatus == COMPLETED || 
                           newStatus == FAILED || newStatus == CANCELLED || newStatus == EXPIRED;
            case PROCESSING -> newStatus == COMPLETED || newStatus == FAILED;
            case COMPLETED -> newStatus == REFUNDED;
            case FAILED, CANCELLED, REFUNDED, EXPIRED -> false;
        };
    }
    
    public boolean isActive() {
        return this == PENDING || this == PROCESSING;
    }
    
    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED || 
               this == REFUNDED || this == EXPIRED;
    }
}
