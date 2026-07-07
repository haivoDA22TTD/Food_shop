package com.example.foodshop.order.dto;

import jakarta.validation.constraints.NotNull;

public class AssignShipperRequest {
    
    @NotNull(message = "Shipper ID is required")
    private Long shipperId;
    
    private String notes;
    
    // Constructors
    public AssignShipperRequest() {
    }
    
    public AssignShipperRequest(Long shipperId) {
        this.shipperId = shipperId;
    }
    
    public AssignShipperRequest(Long shipperId, String notes) {
        this.shipperId = shipperId;
        this.notes = notes;
    }
    
    // Getters and Setters
    public Long getShipperId() {
        return shipperId;
    }
    
    public void setShipperId(Long shipperId) {
        this.shipperId = shipperId;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
