package com.example.foodshop.order.controller;

import com.example.foodshop.order.dto.OrderResponse;
import com.example.foodshop.order.dto.OrderStatisticsResponse;
import com.example.foodshop.order.dto.OrderStatusUpdateRequest;
import com.example.foodshop.order.entity.OrderStatus;
import com.example.foodshop.order.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {
    
    private static final Logger log = LoggerFactory.getLogger(AdminOrderController.class);
    
    @Autowired
    private OrderService orderService;
    
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) Long userId) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderResponse> orders = orderService.getAllOrders(pageable, status, orderNumber, userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error getting all orders: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId,
                                             @Valid @RequestBody OrderStatusUpdateRequest request) {
        try {
            OrderResponse order = orderService.updateOrderStatus(orderId, request);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid order status update: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating order {} status: {}", orderId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to update order status"));
        }
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<?> getOrderStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            OrderStatisticsResponse statistics = orderService.getOrderStatistics(startDate, endDate);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting order statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to retrieve order statistics"));
        }
    }
}