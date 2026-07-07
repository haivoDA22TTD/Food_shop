package com.example.foodshop.order.controller;

import com.example.foodshop.order.dto.UserAddressResponse;
import com.example.foodshop.order.entity.UserAddress;
import com.example.foodshop.order.repository.UserAddressRepository;
import com.example.foodshop.order.security.OrderUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/addresses")
@PreAuthorize("isAuthenticated()")
public class UserAddressController {

    private static final Logger log = LoggerFactory.getLogger(UserAddressController.class);

    @Autowired
    private UserAddressRepository userAddressRepository;

    @GetMapping
    public ResponseEntity<List<UserAddressResponse>> getAddresses(Authentication auth) {
        OrderUserDetails userDetails = (OrderUserDetails) auth.getPrincipal();
        List<UserAddressResponse> addresses = userAddressRepository
            .findByUserIdOrderByCreatedAtDesc(userDetails.getUserId())
            .stream()
            .map(UserAddressResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(addresses);
    }

    @PostMapping
    public ResponseEntity<?> saveAddress(@RequestBody Map<String, Object> request, Authentication auth) {
        OrderUserDetails userDetails = (OrderUserDetails) auth.getPrincipal();
        Long userId = userDetails.getUserId();

        String fullAddress = (String) request.get("fullAddress");
        Integer provinceCode = (Integer) request.get("provinceCode");
        Integer districtCode = (Integer) request.get("districtCode");
        Integer wardCode = (Integer) request.get("wardCode");
        String street = (String) request.get("street");
        String phoneNumber = (String) request.get("phoneNumber");
        String label = (String) request.get("label");

        if (fullAddress == null || fullAddress.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Full address is required"));
        }

        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setFullAddress(fullAddress);
        address.setProvinceCode(provinceCode);
        address.setDistrictCode(districtCode);
        address.setWardCode(wardCode);
        address.setStreet(street);
        address.setPhoneNumber(phoneNumber);
        address.setLabel(label);

        if (Boolean.TRUE.equals(request.get("isDefault"))) {
            userAddressRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .forEach(a -> a.setIsDefault(false));
            address.setIsDefault(true);
        }

        UserAddress saved = userAddressRepository.save(address);
        log.info("User {} saved address #{}", userId, saved.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(new UserAddressResponse(saved));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long addressId, Authentication auth) {
        OrderUserDetails userDetails = (OrderUserDetails) auth.getPrincipal();

        var addressOpt = userAddressRepository.findByIdAndUserId(addressId, userDetails.getUserId());
        if (addressOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        userAddressRepository.delete(addressOpt.get());
        log.info("User {} deleted address #{}", userDetails.getUserId(), addressId);
        return ResponseEntity.ok(Map.of("message", "Address deleted"));
    }
}
