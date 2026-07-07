package com.example.foodshop.order.dto;

import com.example.foodshop.order.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public class OrderTrackingResponse {
    
    private String orderNumber;
    private OrderStatus currentStatus;
    private String statusDisplayName;
    private LocalDateTime estimatedDelivery;
    private List<OrderStatusHistory> statusHistory;
    
    // Constructors
    public OrderTrackingResponse() {
    }
    
    public OrderTrackingResponse(String orderNumber, OrderStatus currentStatus, String statusDisplayName,
                                LocalDateTime estimatedDelivery, List<OrderStatusHistory> statusHistory) {
        this.orderNumber = orderNumber;
        this.currentStatus = currentStatus;
        this.statusDisplayName = statusDisplayName;
        this.estimatedDelivery = estimatedDelivery;
        this.statusHistory = statusHistory;
    }
    
    // Getters and Setters
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }
    
    public void setCurrentStatus(OrderStatus currentStatus) {
        this.currentStatus = currentStatus;
    }
    
    public String getStatusDisplayName() {
        return statusDisplayName;
    }
    
    public void setStatusDisplayName(String statusDisplayName) {
        this.statusDisplayName = statusDisplayName;
    }
    
    public LocalDateTime getEstimatedDelivery() {
        return estimatedDelivery;
    }
    
    public void setEstimatedDelivery(LocalDateTime estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
    }
    
    public List<OrderStatusHistory> getStatusHistory() {
        return statusHistory;
    }
    
    public void setStatusHistory(List<OrderStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }
    
    public static class OrderStatusHistory {
        private OrderStatus status;
        private String statusDisplayName;
        private LocalDateTime timestamp;
        private String description;
        
        // Constructors
        public OrderStatusHistory() {
        }
        
        public OrderStatusHistory(OrderStatus status, String statusDisplayName, LocalDateTime timestamp, String description) {
            this.status = status;
            this.statusDisplayName = statusDisplayName;
            this.timestamp = timestamp;
            this.description = description;
        }
        
        // Getters and Setters
        public OrderStatus getStatus() {
            return status;
        }
        
        public void setStatus(OrderStatus status) {
            this.status = status;
        }
        
        public String getStatusDisplayName() {
            return statusDisplayName;
        }
        
        public void setStatusDisplayName(String statusDisplayName) {
            this.statusDisplayName = statusDisplayName;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }
}