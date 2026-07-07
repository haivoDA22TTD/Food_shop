package com.example.foodshop.payment.service;

import com.example.foodshop.payment.config.FeignConfig;
import com.example.foodshop.payment.dto.response.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order-service", configuration = FeignConfig.class)
public interface OrderFeignClient {
    
    @GetMapping("/api/orders/{orderId}")
    OrderResponse getOrderById(@PathVariable("orderId") Long orderId);
    
    @PutMapping("/api/admin/orders/{orderId}/status")
    OrderResponse updateOrderStatus(@PathVariable("orderId") Long orderId, 
                                   @RequestBody OrderStatusUpdateRequest request);
    
    class OrderStatusUpdateRequest {
        private String status;
        private String reason;
        
        public OrderStatusUpdateRequest() {
        }
        
        public OrderStatusUpdateRequest(String status, String reason) {
            this.status = status;
            this.reason = reason;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
