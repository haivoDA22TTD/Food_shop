package com.example.foodshop.controller;

import com.example.foodshop.dto.AuthRequest;
import com.example.foodshop.dto.AuthResponse;
import com.example.foodshop.entity.User;
import com.example.foodshop.security.JwtUtil;
import com.example.foodshop.service.CustomUserDetailsService;
import com.example.foodshop.service.TokenBlacklistService;
import com.example.foodshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        
        return ResponseEntity.ok(new AuthResponse(token, request.getUsername(), role));
    }
    
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        userService.registerUser(user);
        return ResponseEntity.ok("Đăng ký thành công");
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid token"));
        }
        
        String token = authHeader.substring(7);
        
        try {
            // Extract expiration time from token
            Date expirationDate = jwtUtil.extractExpiration(token);
            long currentTime = System.currentTimeMillis();
            long expirationTime = expirationDate.getTime();
            
            // Calculate remaining time until token expires
            long remainingTime = (expirationTime - currentTime) / 1000; // Convert to seconds
            
            if (remainingTime > 0) {
                // Add token to blacklist with remaining expiration time
                tokenBlacklistService.blacklistToken(token, remainingTime);
                return ResponseEntity.ok(Map.of(
                    "message", "Đăng xuất thành công",
                    "tokenBlacklisted", true
                ));
            } else {
                // Token already expired
                return ResponseEntity.ok(Map.of(
                    "message", "Token đã hết hạn",
                    "tokenBlacklisted", false
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid token: " + e.getMessage()
            ));
        }
    }
}
