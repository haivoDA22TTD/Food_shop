package com.example.foodshop.order.controller;

import com.example.foodshop.order.dto.OrderResponse;
import com.example.foodshop.order.dto.ShipperResponse;
import com.example.foodshop.order.entity.OrderStatus;
import com.example.foodshop.order.service.OrderService;
import com.example.foodshop.order.service.ShipperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/shipper")
@PreAuthorize("hasRole('SHIPPER')")
public class ShipperOrderController {
    
    private static final Logger log = LoggerFactory.getLogger(ShipperOrderController.class);
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ShipperService shipperService;
    
    /**
     * Get current shipper profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            log.info("Shipper {} getting profile", userId);
            
            ShipperResponse shipper = shipperService.getShipperByUserId(userId);
            return ResponseEntity.ok(shipper);
        } catch (IllegalArgumentException e) {
            log.warn("Shipper profile not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting shipper profile: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to retrieve profile"));
        }
    }
    
    /**
     * Get orders assigned to current shipper
     */
    @GetMapping("/orders")
    public ResponseEntity<?> getMyOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) OrderStatus status) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            log.info("Shipper {} getting orders - status: {}", userId, status);
            
            // Get shipper by userId
            ShipperResponse shipper = shipperService.getShipperByUserId(userId);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderResponse> orders = orderService.getOrdersByShipper(
                    shipper.getId(), pageable, status);
            
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            log.warn("Shipper not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting shipper orders: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to retrieve orders"));
        }
    }
    
    /**
     * Get order detail
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> getOrderDetail(
            Authentication authentication,
            @PathVariable Long orderId) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            log.info("Shipper {} getting order detail: {}", userId, orderId);
            
            // Get shipper by userId
            ShipperResponse shipper = shipperService.getShipperByUserId(userId);
            
            // Get order and verify it's assigned to this shipper
            OrderResponse order = orderService.getOrderByIdForShipper(orderId, shipper.getId());
            
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            log.warn("Order not found or not assigned to shipper: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting order detail: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to retrieve order"));
        }
    }
    
    /**
     * Mark order as picked up
     */
    @PutMapping("/orders/{orderId}/pickup")
    public ResponseEntity<?> markAsPickedUp(
            Authentication authentication,
            @PathVariable Long orderId) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            log.info("Shipper {} marking order {} as picked up", userId, orderId);
            
            // Get shipper by userId
            ShipperResponse shipper = shipperService.getShipperByUserId(userId);
            
            // Mark as picked up
            OrderResponse order = orderService.markOrderAsPickedUpByShipper(orderId, shipper.getId());
            
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot mark order as picked up: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error marking order as picked up: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to update order"));
        }
    }
    
    /**
     * Mark order as delivered
     */
    @PutMapping("/orders/{orderId}/deliver")
    public ResponseEntity<?> markAsDelivered(
            Authentication authentication,
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            log.info("Shipper {} marking order {} as delivered", userId, orderId);
            
            // Get shipper by userId
            ShipperResponse shipper = shipperService.getShipperByUserId(userId);
            
            String notes = body != null ? body.get("notes") : null;
            
            // Mark as delivered
            OrderResponse order = orderService.markOrderAsDeliveredByShipper(
                    orderId, shipper.getId(), notes);
            
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot mark order as delivered: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error marking order as delivered: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to update order"));
        }
    }
    
    /**
     * Add delivery notes
     */
    @PutMapping("/orders/{orderId}/notes")
    public ResponseEntity<?> addDeliveryNotes(
            Authentication authentication,
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            String notes = body.get("notes");
            log.info("Shipper {} adding notes to order {}", userId, orderId);
            
            // Get shipper by userId
            ShipperResponse shipper = shipperService.getShipperByUserId(userId);
            
            // Add notes
            OrderResponse order = orderService.addDeliveryNotesByShipper(
                    orderId, shipper.getId(), notes);
            
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot add delivery notes: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error adding delivery notes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to update order"));
        }
    }
    
    /**
     * Get shipper statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getMyStatistics(Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            log.info("Shipper {} getting statistics", userId);
            
            // Get shipper by userId
            ShipperResponse shipper = shipperService.getShipperByUserId(userId);
            
            return ResponseEntity.ok(shipper);
        } catch (IllegalArgumentException e) {
            log.warn("Shipper not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting shipper statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to retrieve statistics"));
        }
    }
}
