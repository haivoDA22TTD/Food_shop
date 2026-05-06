package com.example.foodshop.order.controller;

import com.example.foodshop.order.dto.CreateOrderRequest;
import com.example.foodshop.order.dto.OrderResponse;
import com.example.foodshop.order.dto.OrderTrackingResponse;
import com.example.foodshop.order.entity.OrderStatus;
import com.example.foodshop.order.security.OrderUserDetails;
import com.example.foodshop.order.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    
    @Autowired
    private OrderService orderService;
    
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status,
            Authentication auth) {
        try {
            OrderUserDetails userDetails = (OrderUserDetails) auth.getPrincipal();
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderResponse> orders = orderService.getUserOrders(userDetails.getUserId(), pageable, status);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error getting user orders: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId, Authentication auth) {
        try {
            OrderUserDetails userDetails = (OrderUserDetails) auth.getPrincipal();
            OrderResponse order = orderService.getOrderById(orderId, userDetails.getUserId());
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            log.warn("Order access denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Order not found"));
        } catch (Exception e) {
            log.error("Error getting order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to retrieve order"));
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest request, Authentication auth) {
        try {
            OrderUserDetails userDetails = (OrderUserDetails) auth.getPrincipal();
            OrderResponse order = orderService.createOrderFromCart(userDetails.getUserId(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid create order request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating order: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to create order"));
        }
    }
    
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId, Authentication auth) {
        try {
            OrderUserDetails userDetails = (OrderUserDetails) auth.getPrincipal();
            OrderResponse order = orderService.cancelOrder(orderId, userDetails.getUserId());
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid cancel order request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error cancelling order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to cancel order"));
        }
    }
    
    @GetMapping("/{orderId}/tracking")
    public ResponseEntity<?> getOrderTracking(@PathVariable Long orderId, Authentication auth) {
        try {
            OrderUserDetails userDetails = (OrderUserDetails) auth.getPrincipal();
            OrderTrackingResponse tracking = orderService.getOrderTracking(orderId, userDetails.getUserId());
            return ResponseEntity.ok(tracking);
        } catch (IllegalArgumentException e) {
            log.warn("Order tracking access denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Order not found"));
        } catch (Exception e) {
            log.error("Error getting order tracking {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to retrieve order tracking"));
        }
    }
}