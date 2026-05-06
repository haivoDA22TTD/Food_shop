package com.example.foodshop.order.controller;

import com.example.foodshop.order.dto.CartItemRequest;
import com.example.foodshop.order.dto.CartResponse;
import com.example.foodshop.order.dto.CartSummaryResponse;
import com.example.foodshop.order.security.OrderUserDetails;
import com.example.foodshop.order.service.CartService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    
    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    
    @Autowired
    private CartService cartService;
    
    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication auth) {
        try {
            OrderUserDetails userDetails = (OrderUserDetails) auth.getPrincipal();
            CartResponse cart = cartService.getOrCreateCart(userDetails.getUserId());
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            log.error("Error getting cart: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(null);
        }
    }
    
    @PostMapping("/items")
    public ResponseEntity<?> addToCart(@Valid @RequestBody CartItemRequest request, Authentication auth) {
        try {
            OrderUserDetails userDetails = (OrderUserDetails) auth.getPrincipal();
            CartResponse cart = cartService.addToCart(userDetails.getUserId(), request);
            return ResponseEntity.ok(cart);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid add to cart request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error adding to cart: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to add item to cart"));
        }
    }
    
    @PutMapping("/items/{productId}")
    public ResponseEntity<?> updateCartItem(@PathVariable Long productId,
                                          @Valid @RequestBody CartItemRequest request,
                                          Authentication auth) {
        try {
            OrderUserDetails userDetails = (OrderUserDetails) auth.getPrincipal();
            CartResponse cart = cartService.updateCartItem(userDetails.getUserId(), productId, request);
            return ResponseEntity.ok(cart);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid update cart item request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating cart item: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to update cart item"));
        }
    }
    
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId, Authentication auth) {
        try {
            OrderUserDetails userDetails = (OrderUserDetails) auth.getPrincipal();
            CartResponse cart = cartService.removeFromCart(userDetails.getUserId(), productId);
            return ResponseEntity.ok(cart);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid remove from cart request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error removing from cart: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to remove item from cart"));
        }
    }
    
    @DeleteMapping
    public ResponseEntity<?> clearCart(Authentication auth) {
        try {
            OrderUserDetails userDetails = (OrderUserDetails) auth.getPrincipal();
            cartService.clearCart(userDetails.getUserId());
            return ResponseEntity.ok(Map.of("message", "Cart cleared successfully"));
        } catch (Exception e) {
            log.error("Error clearing cart: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to clear cart"));
        }
    }
    
    @GetMapping("/summary")
    public ResponseEntity<CartSummaryResponse> getCartSummary(Authentication auth) {
        try {
            OrderUserDetails userDetails = (OrderUserDetails) auth.getPrincipal();
            CartSummaryResponse summary = cartService.getCartSummary(userDetails.getUserId());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error getting cart summary: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new CartSummaryResponse(0, null));
        }
    }
}