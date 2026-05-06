package com.example.foodshop.order.entity;

public enum OrderStatus {
    PENDING("Đang chờ xử lý"),
    CONFIRMED("Đã xác nhận"),
    PREPARING("Đang chuẩn bị"),
    READY_FOR_PICKUP("Sẵn sàng lấy hàng"),
    DELIVERED("Đã giao hàng"),
    CANCELLED("Đã hủy");
    
    private final String displayName;
    
    OrderStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isActive() {
        return this != CANCELLED && this != DELIVERED;
    }
    
    public boolean canTransitionTo(OrderStatus newStatus) {
        switch (this) {
            case PENDING:
                return newStatus == CONFIRMED || newStatus == CANCELLED;
            case CONFIRMED:
                return newStatus == PREPARING || newStatus == CANCELLED;
            case PREPARING:
                return newStatus == READY_FOR_PICKUP || newStatus == CANCELLED;
            case READY_FOR_PICKUP:
                return newStatus == DELIVERED;
            case DELIVERED:
            case CANCELLED:
                return false;
            default:
                return false;
        }
    }
}