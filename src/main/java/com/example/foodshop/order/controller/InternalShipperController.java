package com.example.foodshop.order.controller;

import com.example.foodshop.order.dto.ShipperRequest;
import com.example.foodshop.order.service.ShipperService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal/shippers")
public class InternalShipperController {

    private static final Logger log = LoggerFactory.getLogger(InternalShipperController.class);

    @Autowired
    private ShipperService shipperService;

    @PostMapping
    public ResponseEntity<?> createShipper(@RequestBody Map<String, Object> request) {
        try {
            log.info("Internal: Creating shipper profile for userId: {}", request.get("userId"));

            ShipperRequest shipperRequest = new ShipperRequest();
            shipperRequest.setName((String) request.get("name"));
            shipperRequest.setPhone((String) request.get("phone"));
            shipperRequest.setEmail((String) request.get("email"));
            shipperRequest.setVehicleType((String) request.get("vehicleType"));
            shipperRequest.setVehicleNumber((String) request.get("vehicleNumber"));

            var shipper = shipperService.createShipper(shipperRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(shipper);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid shipper creation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating shipper: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Unable to create shipper"));
        }
    }
}
