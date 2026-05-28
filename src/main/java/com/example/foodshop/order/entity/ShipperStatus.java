package com.example.foodshop.order.entity;

public enum ShipperStatus {
    AVAILABLE("Sẵn sàng"),
    BUSY("Đang giao hàng"),
    OFFLINE("Ngoại tuyến"),
    ON_BREAK("Đang nghỉ");
    
    private final String displayName;
    
    ShipperStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean canAcceptOrder() {
        return this == AVAILABLE;
    }
}
