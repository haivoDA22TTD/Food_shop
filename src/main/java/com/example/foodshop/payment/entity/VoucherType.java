package com.example.foodshop.payment.entity;

public enum VoucherType {
    PERCENTAGE("Giảm theo phần trăm"),
    FIXED_AMOUNT("Giảm số tiền cố định"),
    FREE_SHIPPING("Miễn phí vận chuyển");
    
    private final String displayName;
    
    VoucherType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
