package com.example.foodshop.order.dto;

import com.example.foodshop.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    
    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private String statusDisplayName;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String phoneNumber;
    private String notes;
    private List<OrderItemResponse> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Helper method to get status display name
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : null;
    }
}