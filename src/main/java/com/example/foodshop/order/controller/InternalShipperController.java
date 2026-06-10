package com.example.foodshop.order.controller;

import com.example.foodshop.order.dto.CreateShipperProfileRequest;
import com.example.foodshop.order.dto.ShipperResponse;
import com.example.foodshop.order.entity.Shipper;
import com.example.foodshop.order.entity.ShipperStatus;
import com.example.foodshop.order.repository.ShipperRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Internal controller for shipper profile management.
 * Used by Identity Service for inter-service communication.
 * 
 * Validates: Requirements 1.3 - Shipper profile creation
 */
@RestController
@RequestMapping("/internal/shippers")
public class InternalShipperController {
    
    @Autowired
    private ShipperRepository shipperRepository;
    
    /**
     * Creates a shipper profile in the Order Service.
     * Called by Identity Service after creating a user account with role SHIPPER.
     * 
     * @param request The shipper profile creation request
     * @return ShipperResponse with HTTP 201 on success
     */
    @PostMapping
    public ResponseEntity<?> createShipperProfile(@Valid @RequestBody CreateShipperProfileRequest request) {
        try {
            // Validate that userId is not already linked to another shipper
            Optional<Shipper> existingByUserId = shipperRepository.findByUserId(request.getUserId());
            if (existingByUserId.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "UserId already linked to another shipper"));
            }
            
            // Validate that phone number is unique
            Optional<Shipper> existingByPhone = shipperRepository.findByPhone(request.getPhone());
            if (existingByPhone.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Phone number already exists"));
            }
            
            // Create Shipper entity with default values
            Shipper shipper = new Shipper();
            shipper.setUserId(request.getUserId());
            shipper.setName(request.getName());
            shipper.setPhone(request.getPhone());
            shipper.setEmail(request.getEmail());
            shipper.setVehicleType(request.getVehicleType());
            shipper.setVehicleNumber(request.getVehicleNumber());
            
            // Set default values as per requirements
            shipper.setStatus(ShipperStatus.AVAILABLE);
            shipper.setIsActive(true);
            shipper.setRating(5.0);
            shipper.setTotalDeliveries(0);
            shipper.setSuccessfulDeliveries(0);
            shipper.setTotalRatings(0);
            
            // Save shipper to database
            Shipper savedShipper = shipperRepository.save(shipper);
            
            // Return ShipperResponse DTO
            ShipperResponse response = new ShipperResponse(savedShipper);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create shipper profile", 
                               "detail", e.getMessage()));
        }
    }
}
