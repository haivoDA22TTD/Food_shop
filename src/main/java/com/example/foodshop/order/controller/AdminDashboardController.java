package com.example.foodshop.order.controller;

import com.example.foodshop.order.dto.DashboardResponse;
import com.example.foodshop.order.service.AdminDashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private static final Logger log = LoggerFactory.getLogger(AdminDashboardController.class);

    @Autowired
    private AdminDashboardService adminDashboardService;

    @GetMapping
    public ResponseEntity<?> getDashboard() {
        try {
            log.info("Admin requesting dashboard data");
            DashboardResponse dashboard = adminDashboardService.getDashboard();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Error building dashboard: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to load dashboard data"));
        }
    }
}
