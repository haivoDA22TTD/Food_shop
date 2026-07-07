package com.example.foodshop.product.controller;

import com.example.foodshop.product.entity.Product;
import com.example.foodshop.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ApiProductController {
    private final ProductService productService;

    public ApiProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}/decrement-stock")
    public ResponseEntity<?> decrementStock(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer quantity = body.getOrDefault("quantity", 1);
        boolean success = productService.decrementStock(id, quantity);
        if (!success) {
            return ResponseEntity.badRequest().body(Map.of("error", "Insufficient stock or product not found"));
        }
        return ResponseEntity.ok(Map.of("message", "Stock decremented successfully"));
    }
}
