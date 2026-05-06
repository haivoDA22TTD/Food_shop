package com.example.foodshop.order.dto;

import com.example.foodshop.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingResponse {
    
    private String orderNumber;
    private OrderStatus currentStatus;
    private String statusDisplayName;
    private LocalDateTime estimatedDelivery;
    private List<OrderStatusHistory> statusHistory;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusHistory {
        private OrderStatus status;
        private String statusDisplayName;
        private LocalDateTime timestamp;
        private String description;
    }
}