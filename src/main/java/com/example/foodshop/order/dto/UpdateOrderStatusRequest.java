package com.example.foodshop.order.dto;

import com.example.foodshop.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating order status by shippers
 * Used in shipper-specific endpoints for delivery status updates
 */
public class UpdateOrderStatusRequest {
    
    @NotNull(message = "Status is required")
    private OrderStatus status;
    
    private String notes; // Optional delivery notes from shipper
    
    // Constructors
    public UpdateOrderStatusRequest() {
    }
    
    public UpdateOrderStatusRequest(OrderStatus status, String notes) {
        this.status = status;
        this.notes = notes;
    }
    
    // Getters and Setters
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
