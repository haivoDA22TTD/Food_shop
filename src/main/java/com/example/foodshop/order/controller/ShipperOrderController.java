package com.example.foodshop.order.controller;

import com.example.foodshop.order.dto.ShipperOrderResponse;
import com.example.foodshop.order.dto.UpdateOrderStatusRequest;
import com.example.foodshop.order.entity.Order;
import com.example.foodshop.order.entity.OrderStatus;
import com.example.foodshop.order.repository.OrderRepository;
import com.example.foodshop.order.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for shipper-facing order endpoints.
 * Allows authenticated shippers to view assigned orders and update delivery status.
 * 
 * Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6
 */
@RestController
@RequestMapping("/api/orders/shipper")
public class ShipperOrderController {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Get orders assigned to the authenticated shipper.
     * Supports filtering by status and pagination.
     * 
     * @param request HTTP request to extract JWT token
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param status Optional status filter
     * @return Paginated list of orders assigned to the shipper
     */
    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) OrderStatus status) {
        
        try {
            // Extract JWT token from Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired token"));
            }
            
            String token = authHeader.substring(7);
            
            // Validate token and extract userId
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired token"));
            }
            
            Long shipperId = jwtUtil.extractUserId(token);
            if (shipperId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token: userId not found"));
            }
            
            // Create pageable request
            Pageable pageable = PageRequest.of(page, size);
            
            // Query orders by shipperId (with optional status filter)
            Page<Order> ordersPage;
            if (status != null) {
                ordersPage = orderRepository.findByShipperIdAndStatusOrderByCreatedAtDesc(
                        shipperId, status, pageable);
            } else {
                ordersPage = orderRepository.findByShipperIdOrderByCreatedAtDesc(
                        shipperId, pageable);
            }
            
            // Map to ShipperOrderResponse DTOs
            Page<ShipperOrderResponse> responsePage = ordersPage.map(ShipperOrderResponse::new);
            
            // Build response with pagination metadata
            Map<String, Object> response = new HashMap<>();
            response.put("content", responsePage.getContent());
            response.put("pageable", Map.of(
                    "pageNumber", responsePage.getNumber(),
                    "pageSize", responsePage.getSize()
            ));
            response.put("totalElements", responsePage.getTotalElements());
            response.put("totalPages", responsePage.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch orders", 
                               "detail", e.getMessage()));
        }
    }
    
    /**
     * Update the status of an order assigned to the authenticated shipper.
     * Validates shipper authorization and status transition rules.
     * 
     * @param request HTTP request to extract JWT token
     * @param orderId The ID of the order to update
     * @param statusRequest The new status and optional notes
     * @return Updated order details
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            HttpServletRequest request,
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest statusRequest) {
        
        try {
            // Extract JWT token from Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired token"));
            }
            
            String token = authHeader.substring(7);
            
            // Validate token and extract userId
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired token"));
            }
            
            Long shipperId = jwtUtil.extractUserId(token);
            if (shipperId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token: userId not found"));
            }
            
            // Fetch order by orderId
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Order not found"));
            }
            
            // Validate that order is assigned to this shipper
            if (order.getShipperId() == null || !order.getShipperId().equals(shipperId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Not authorized to update this order"));
            }
            
            // Validate status transition
            OrderStatus currentStatus = order.getStatus();
            OrderStatus newStatus = statusRequest.getStatus();
            
            if (!isValidStatusTransition(currentStatus, newStatus)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid status transition from " + 
                                   currentStatus + " to " + newStatus));
            }
            
            // Update order status
            order.setStatus(newStatus);
            
            // Set appropriate timestamp based on new status
            if (newStatus == OrderStatus.IN_TRANSIT) {
                order.setPickedUpAt(LocalDateTime.now());
            } else if (newStatus == OrderStatus.DELIVERED) {
                order.setDeliveredAt(LocalDateTime.now());
            }
            
            // Save delivery notes if provided
            if (statusRequest.getNotes() != null && !statusRequest.getNotes().isBlank()) {
                order.setDeliveryNotes(statusRequest.getNotes());
            }
            
            // Update timestamp
            order.setUpdatedAt(LocalDateTime.now());
            
            // Save order to database
            Order updatedOrder = orderRepository.save(order);
            
            // Return updated order
            ShipperOrderResponse response = new ShipperOrderResponse(updatedOrder);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update order status", 
                               "detail", e.getMessage()));
        }
    }
    
    /**
     * Validates status transitions according to business rules.
     * Valid transitions:
     * - ASSIGNED → IN_TRANSIT
     * - IN_TRANSIT → DELIVERED
     * - DELIVERED → (no further transitions allowed)
     * 
     * @param currentStatus The current order status
     * @param newStatus The requested new status
     * @return true if transition is valid, false otherwise
     */
    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Prevent any transition from DELIVERED
        if (currentStatus == OrderStatus.DELIVERED) {
            return false;
        }
        
        // Valid transitions
        if (currentStatus == OrderStatus.ASSIGNED && newStatus == OrderStatus.IN_TRANSIT) {
            return true;
        }
        if (currentStatus == OrderStatus.IN_TRANSIT && newStatus == OrderStatus.DELIVERED) {
            return true;
        }
        
        // Also allow backwards-compatible transitions for orders not yet picked up
        if (currentStatus == OrderStatus.READY_FOR_PICKUP && newStatus == OrderStatus.IN_TRANSIT) {
            return true;
        }
        
        return false;
    }
}
