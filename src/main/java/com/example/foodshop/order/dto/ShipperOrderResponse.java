package com.example.foodshop.order.dto;

import com.example.foodshop.order.entity.Order;
import com.example.foodshop.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for order responses in shipper endpoints
 * Contains order details visible to shippers for delivery
 * 
 * Validates: Requirements 4.2 - Order details for shippers
 */
public class ShipperOrderResponse {
    
    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String phoneNumber;
    private String customerName;
    private List<OrderItemResponse> orderItems;
    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private String deliveryNotes;
    private LocalDateTime createdAt;
    
    // Constructors
    public ShipperOrderResponse() {
    }
    
    public ShipperOrderResponse(Order order) {
        this.id = order.getId();
        this.orderNumber = order.getOrderNumber();
        this.status = order.getStatus();
        this.totalAmount = order.getTotalAmount();
        this.shippingAddress = order.getShippingAddress();
        this.phoneNumber = order.getPhoneNumber();
        this.customerName = extractCustomerNameFromAddress(order.getShippingAddress());
        this.orderItems = order.getOrderItems().stream()
                .map(OrderItemResponse::new)
                .collect(Collectors.toList());
        this.assignedAt = order.getAssignedAt();
        this.pickedUpAt = order.getPickedUpAt();
        this.deliveredAt = order.getDeliveredAt();
        this.deliveryNotes = order.getDeliveryNotes();
        this.createdAt = order.getCreatedAt();
    }
    
    /**
     * Extract customer name from shipping address (if available)
     * This is a simple implementation - can be enhanced with a separate customer name field
     */
    private String extractCustomerNameFromAddress(String address) {
        if (address == null || address.isEmpty()) {
            return "Customer";
        }
        // Try to extract the first line as customer name
        String[] lines = address.split("\n|,");
        if (lines.length > 0) {
            return lines[0].trim();
        }
        return "Customer";
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public List<OrderItemResponse> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItemResponse> orderItems) {
        this.orderItems = orderItems;
    }
    
    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
    
    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
    
    public LocalDateTime getPickedUpAt() {
        return pickedUpAt;
    }
    
    public void setPickedUpAt(LocalDateTime pickedUpAt) {
        this.pickedUpAt = pickedUpAt;
    }
    
    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }
    
    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
    
    public String getDeliveryNotes() {
        return deliveryNotes;
    }
    
    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
