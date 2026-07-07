package com.example.foodshop.order.service;

import com.example.foodshop.order.dto.*;
import com.example.foodshop.order.entity.Shipper;
import com.example.foodshop.order.entity.ShipperStatus;
import com.example.foodshop.order.repository.ShipperRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShipperService {
    
    private static final Logger log = LoggerFactory.getLogger(ShipperService.class);
    
    @Autowired
    private ShipperRepository shipperRepository;
    
    /**
     * Create a new shipper
     */
    @Transactional
    public ShipperResponse createShipper(ShipperRequest request) {
        log.info("Creating new shipper: {}", request.getName());
        
        // Check if phone already exists
        if (shipperRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Phone number already exists");
        }
        
        Shipper shipper = new Shipper();
        shipper.setName(request.getName());
        shipper.setPhone(request.getPhone());
        shipper.setEmail(request.getEmail());
        shipper.setAddress(request.getAddress());
        shipper.setVehicleType(request.getVehicleType());
        shipper.setVehicleNumber(request.getVehicleNumber());
        shipper.setNotes(request.getNotes());
        
        Shipper savedShipper = shipperRepository.save(shipper);
        log.info("Shipper created successfully with ID: {}", savedShipper.getId());
        
        return new ShipperResponse(savedShipper);
    }
    
    /**
     * Update shipper information
     */
    @Transactional
    public ShipperResponse updateShipper(Long shipperId, ShipperRequest request) {
        log.info("Updating shipper ID: {}", shipperId);
        
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new IllegalArgumentException("Shipper not found"));
        
        // Check if phone is being changed and if it already exists
        if (!shipper.getPhone().equals(request.getPhone())) {
            if (shipperRepository.findByPhone(request.getPhone()).isPresent()) {
                throw new IllegalArgumentException("Phone number already exists");
            }
        }
        
        shipper.setName(request.getName());
        shipper.setPhone(request.getPhone());
        shipper.setEmail(request.getEmail());
        shipper.setAddress(request.getAddress());
        shipper.setVehicleType(request.getVehicleType());
        shipper.setVehicleNumber(request.getVehicleNumber());
        shipper.setNotes(request.getNotes());
        
        Shipper updatedShipper = shipperRepository.save(shipper);
        log.info("Shipper updated successfully: {}", shipperId);
        
        return new ShipperResponse(updatedShipper);
    }
    
    /**
     * Update shipper status
     */
    @Transactional
    public ShipperResponse updateShipperStatus(Long shipperId, ShipperStatusUpdateRequest request) {
        log.info("Updating shipper {} status to: {}", shipperId, request.getStatus());
        
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new IllegalArgumentException("Shipper not found"));
        
        shipper.setStatus(request.getStatus());
        Shipper updatedShipper = shipperRepository.save(shipper);
        
        log.info("Shipper status updated successfully");
        return new ShipperResponse(updatedShipper);
    }
    
    /**
     * Activate/Deactivate shipper
     */
    @Transactional
    public ShipperResponse toggleShipperActive(Long shipperId) {
        log.info("Toggling shipper {} active status", shipperId);
        
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new IllegalArgumentException("Shipper not found"));
        
        shipper.setIsActive(!shipper.getIsActive());
        
        // If deactivating, set status to OFFLINE
        if (!shipper.getIsActive()) {
            shipper.setStatus(ShipperStatus.OFFLINE);
        }
        
        Shipper updatedShipper = shipperRepository.save(shipper);
        log.info("Shipper active status toggled to: {}", updatedShipper.getIsActive());
        
        return new ShipperResponse(updatedShipper);
    }
    
    /**
     * Get shipper by ID
     */
    public ShipperResponse getShipperById(Long shipperId) {
        log.info("Getting shipper by ID: {}", shipperId);
        
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new IllegalArgumentException("Shipper not found"));
        
        return new ShipperResponse(shipper);
    }
    
    /**
     * Get all shippers with pagination and filters
     */
    public Page<ShipperResponse> getAllShippers(Pageable pageable, ShipperStatus status, String search) {
        log.info("Getting all shippers - page: {}, size: {}, status: {}, search: {}", 
                 pageable.getPageNumber(), pageable.getPageSize(), status, search);
        
        Page<Shipper> shippers;
        
        if (status != null) {
            shippers = shipperRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else if (search != null && !search.trim().isEmpty()) {
            // Search by name or phone
            if (search.matches("^[0-9]+$")) {
                shippers = shipperRepository.findByPhoneContainingOrderByCreatedAtDesc(search, pageable);
            } else {
                shippers = shipperRepository.findByNameContainingIgnoreCaseOrderByCreatedAtDesc(search, pageable);
            }
        } else {
            shippers = shipperRepository.findAll(pageable);
        }
        
        return shippers.map(ShipperResponse::new);
    }
    
    /**
     * Get available shippers for assignment
     */
    public List<ShipperResponse> getAvailableShippers() {
        log.info("Getting available shippers");
        
        List<Shipper> shippers = shipperRepository.findAvailableShippers();
        
        return shippers.stream()
                .map(ShipperResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Get top rated shippers
     */
    public List<ShipperResponse> getTopRatedShippers(int limit) {
        log.info("Getting top {} rated shippers", limit);
        
        Pageable pageable = PageRequest.of(0, limit);
        List<Shipper> shippers = shipperRepository.findTopRatedShippers(pageable);
        
        return shippers.stream()
                .map(ShipperResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Get shipper statistics
     */
    public ShipperStatisticsResponse getShipperStatistics() {
        log.info("Getting shipper statistics");
        
        ShipperStatisticsResponse stats = new ShipperStatisticsResponse();
        
        // Get counts
        stats.setTotalShippers(shipperRepository.count());
        stats.setActiveShippers(shipperRepository.countByIsActiveTrue());
        stats.setAvailableShippers(shipperRepository.countByStatus(ShipperStatus.AVAILABLE));
        stats.setBusyShippers(shipperRepository.countByStatus(ShipperStatus.BUSY));
        
        // Get aggregated statistics
        Object[] aggregatedStats = shipperRepository.getShipperStatistics();
        if (aggregatedStats != null && aggregatedStats.length > 0) {
            stats.setAverageRating(aggregatedStats[1] != null ? (Double) aggregatedStats[1] : 0.0);
            stats.setTotalDeliveries(aggregatedStats[2] != null ? ((Number) aggregatedStats[2]).longValue() : 0L);
            stats.setSuccessfulDeliveries(aggregatedStats[3] != null ? ((Number) aggregatedStats[3]).longValue() : 0L);
            
            // Calculate overall success rate
            if (stats.getTotalDeliveries() > 0) {
                double successRate = (stats.getSuccessfulDeliveries() * 100.0) / stats.getTotalDeliveries();
                stats.setOverallSuccessRate(successRate);
            } else {
                stats.setOverallSuccessRate(100.0);
            }
        }
        
        return stats;
    }
    
    /**
     * Delete shipper (soft delete by deactivating)
     */
    @Transactional
    public void deleteShipper(Long shipperId) {
        log.info("Deleting shipper ID: {}", shipperId);
        
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new IllegalArgumentException("Shipper not found"));
        
        // Soft delete by deactivating
        shipper.setIsActive(false);
        shipper.setStatus(ShipperStatus.OFFLINE);
        shipperRepository.save(shipper);
        
        log.info("Shipper deactivated successfully");
    }
    
    /**
     * Update shipper rating
     */
    @Transactional
    public void updateShipperRating(Long shipperId, double rating) {
        log.info("Updating shipper {} rating: {}", shipperId, rating);
        
        if (rating < 1.0 || rating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 1.0 and 5.0");
        }
        
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new IllegalArgumentException("Shipper not found"));
        
        shipper.updateRating(rating);
        shipperRepository.save(shipper);
        
        log.info("Shipper rating updated successfully");
    }
    
    /**
     * Increment shipper deliveries
     */
    @Transactional
    public void incrementShipperDeliveries(Long shipperId, boolean successful) {
        log.info("Incrementing shipper {} deliveries - successful: {}", shipperId, successful);
        
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new IllegalArgumentException("Shipper not found"));
        
        if (successful) {
            shipper.incrementSuccessfulDeliveries();
        } else {
            shipper.incrementDeliveries();
        }
        
        shipperRepository.save(shipper);
        log.info("Shipper deliveries updated successfully");
    }
    
    /**
     * Get shipper by user ID
     */
    public ShipperResponse getShipperByUserId(Long userId) {
        log.info("Getting shipper by user ID: {}", userId);
        
        Shipper shipper = shipperRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Shipper not found for user ID: " + userId));
        
        return new ShipperResponse(shipper);
    }
    
    /**
     * Link shipper to user account
     */
    @Transactional
    public ShipperResponse linkShipperToUser(Long shipperId, Long userId) {
        log.info("Linking shipper {} to user {}", shipperId, userId);
        
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new IllegalArgumentException("Shipper not found"));
        
        // Check if user is already linked to another shipper
        if (shipperRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("User is already linked to a shipper");
        }
        
        shipper.setUserId(userId);
        Shipper updatedShipper = shipperRepository.save(shipper);
        
        log.info("Shipper {} linked to user {} successfully", shipperId, userId);
        return new ShipperResponse(updatedShipper);
    }
}
