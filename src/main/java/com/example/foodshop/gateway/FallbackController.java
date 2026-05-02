package com.example.foodshop.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping("/fallback/auth")
    public ResponseEntity<Map<String, String>> authFallbackGet() {
        return fallbackResponse();
    }

    @PostMapping("/fallback/auth")
    public ResponseEntity<Map<String, String>> authFallbackPost() {
        return fallbackResponse();
    }

    @GetMapping("/fallback/order")
    public ResponseEntity<Map<String, String>> orderFallbackGet() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Order service is temporarily unavailable. Please try again."));
    }

    @PostMapping("/fallback/order")
    public ResponseEntity<Map<String, String>> orderFallbackPost() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Order service is temporarily unavailable. Please try again."));
    }
    
    @GetMapping("/fallback/product")
    public ResponseEntity<Map<String, String>> productFallbackGet() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Product service is temporarily unavailable. Please try again."));
    }

    @PostMapping("/fallback/product")
    public ResponseEntity<Map<String, String>> productFallbackPost() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Product service is temporarily unavailable. Please try again."));
    }

    private ResponseEntity<Map<String, String>> fallbackResponse() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "message", "Authentication service is temporarily unavailable. Please try again."
                ));
    }
}
