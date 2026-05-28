package com.example.foodshop.order.controller;

import com.example.foodshop.order.dto.*;
import com.example.foodshop.order.entity.ShipperStatus;
import com.example.foodshop.order.service.ShipperService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/shippers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminShipperController {
    
    private static final Logger log = LoggerFactory.getLogger(AdminShipperController.class);
    
    @Autowired
    private ShipperService shipperService;
    
    /**
     * Create a new shipper
     */
    @PostMapping
    public ResponseEntity<?> createShipper(@Valid @RequestBody ShipperRequest request) {
        try {
            log.info("Admin creating new shipper: {}", request.getName());
            ShipperResponse shipper = shipperService.createShipper(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(shipper);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid shipper creation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating shipper: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to create shipper"));
        }
    }
    
    /**
     * Update shipper information
     */
    @PutMapping("/{shipperId}")
    public ResponseEntity<?> updateShipper(@PathVariable Long shipperId,
                                          @Valid @RequestBody ShipperRequest request) {
        try {
            log.info("Admin updating shipper ID: {}", shipperId);
            ShipperResponse shipper = shipperService.updateShipper(shipperId, request);
            return ResponseEntity.ok(shipper);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid shipper update: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating shipper {}: {}", shipperId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to update shipper"));
        }
    }
    
    /**
     * Update shipper status
     */
    @PutMapping("/{shipperId}/status")
    public ResponseEntity<?> updateShipperStatus(@PathVariable Long shipperId,
                                                @Valid @RequestBody ShipperStatusUpdateRequest request) {
        try {
            log.info("Admin updating shipper {} status to: {}", shipperId, request.getStatus());
            ShipperResponse shipper = shipperService.updateShipperStatus(shipperId, request);
            return ResponseEntity.ok(shipper);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid shipper status update: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating shipper {} status: {}", shipperId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to update shipper status"));
        }
    }
    
    /**
     * Toggle shipper active status
     */
    @PutMapping("/{shipperId}/toggle-active")
    public ResponseEntity<?> toggleShipperActive(@PathVariable Long shipperId) {
        try {
            log.info("Admin toggling shipper {} active status", shipperId);
            ShipperResponse shipper = shipperService.toggleShipperActive(shipperId);
            return ResponseEntity.ok(shipper);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid shipper toggle: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error toggling shipper {} active status: {}", shipperId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to toggle shipper status"));
        }
    }
    
    /**
     * Get shipper by ID
     */
    @GetMapping("/{shipperId}")
    public ResponseEntity<?> getShipperById(@PathVariable Long shipperId) {
        try {
            log.info("Admin getting shipper by ID: {}", shipperId);
            ShipperResponse shipper = shipperService.getShipperById(shipperId);
            return ResponseEntity.ok(shipper);
        } catch (IllegalArgumentException e) {
            log.warn("Shipper not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting shipper {}: {}", shipperId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to retrieve shipper"));
        }
    }
    
    /**
     * Get all shippers with pagination and filters
     */
    @GetMapping
    public ResponseEntity<?> getAllShippers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ShipperStatus status,
            @RequestParam(required = false) String search) {
        try {
            log.info("Admin getting all shippers - page: {}, size: {}, status: {}, search: {}", 
                     page, size, status, search);
            Pageable pageable = PageRequest.of(page, size);
            Page<ShipperResponse> shippers = shipperService.getAllShippers(pageable, status, search);
            return ResponseEntity.ok(shippers);
        } catch (Exception e) {
            log.error("Error getting all shippers: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to retrieve shippers"));
        }
    }
    
    /**
     * Get available shippers for assignment
     */
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableShippers() {
        try {
            log.info("Admin getting available shippers");
            List<ShipperResponse> shippers = shipperService.getAvailableShippers();
            return ResponseEntity.ok(shippers);
        } catch (Exception e) {
            log.error("Error getting available shippers: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to retrieve available shippers"));
        }
    }
    
    /**
     * Get top rated shippers
     */
    @GetMapping("/top-rated")
    public ResponseEntity<?> getTopRatedShippers(@RequestParam(defaultValue = "10") int limit) {
        try {
            log.info("Admin getting top {} rated shippers", limit);
            List<ShipperResponse> shippers = shipperService.getTopRatedShippers(limit);
            return ResponseEntity.ok(shippers);
        } catch (Exception e) {
            log.error("Error getting top rated shippers: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to retrieve top rated shippers"));
        }
    }
    
    /**
     * Get shipper statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getShipperStatistics() {
        try {
            log.info("Admin getting shipper statistics");
            ShipperStatisticsResponse statistics = shipperService.getShipperStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting shipper statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to retrieve shipper statistics"));
        }
    }
    
    /**
     * Delete shipper (soft delete)
     */
    @DeleteMapping("/{shipperId}")
    public ResponseEntity<?> deleteShipper(@PathVariable Long shipperId) {
        try {
            log.info("Admin deleting shipper ID: {}", shipperId);
            shipperService.deleteShipper(shipperId);
            return ResponseEntity.ok(Map.of("message", "Shipper deactivated successfully"));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid shipper deletion: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting shipper {}: {}", shipperId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to delete shipper"));
        }
    }
}
