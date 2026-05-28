package com.example.foodshop.order.dto;

import com.example.foodshop.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    
    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private String statusDisplayName;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String phoneNumber;
    private String notes;
    private String paymentMethod;
    private String paymentNumber;
    private Boolean autoConfirmed;
    private String cancellationReason;
    private List<OrderItemResponse> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // User information
    private Long userId;
    private String username;
    private String userEmail;
    
    // Constructors
    public OrderResponse() {
    }
    
    public OrderResponse(Long id, String orderNumber, OrderStatus status, String statusDisplayName,
                        BigDecimal totalAmount, String shippingAddress, String phoneNumber, String notes,
                        String paymentMethod, List<OrderItemResponse> orderItems, LocalDateTime createdAt, 
                        LocalDateTime updatedAt, Long userId, String username, String userEmail) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.status = status;
        this.statusDisplayName = statusDisplayName;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.phoneNumber = phoneNumber;
        this.notes = notes;
        this.paymentMethod = paymentMethod;
        this.orderItems = orderItems;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userId = userId;
        this.username = username;
        this.userEmail = userEmail;
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
    
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : null;
    }
    
    public void setStatusDisplayName(String statusDisplayName) {
        this.statusDisplayName = statusDisplayName;
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
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getPaymentNumber() {
        return paymentNumber;
    }
    
    public void setPaymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }
    
    public Boolean getAutoConfirmed() {
        return autoConfirmed;
    }
    
    public void setAutoConfirmed(Boolean autoConfirmed) {
        this.autoConfirmed = autoConfirmed;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
    
    public List<OrderItemResponse> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItemResponse> orderItems) {
        this.orderItems = orderItems;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}