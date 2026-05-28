package com.example.foodshop.order.dto;

import com.example.foodshop.order.entity.ShipperStatus;
import jakarta.validation.constraints.NotNull;

public class ShipperStatusUpdateRequest {
    
    @NotNull(message = "Status is required")
    private ShipperStatus status;
    
    // Constructors
    public ShipperStatusUpdateRequest() {
    }
    
    public ShipperStatusUpdateRequest(ShipperStatus status) {
        this.status = status;
    }
    
    // Getters and Setters
    public ShipperStatus getStatus() {
        return status;
    }
    
    public void setStatus(ShipperStatus status) {
        this.status = status;
    }
}
