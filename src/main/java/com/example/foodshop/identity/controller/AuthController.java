package com.example.foodshop.identity.controller;

import com.example.foodshop.identity.dto.AuthRequest;
import com.example.foodshop.identity.dto.AuthResponse;
import com.example.foodshop.identity.dto.RegisterRequest;
import com.example.foodshop.identity.entity.User;
import com.example.foodshop.identity.security.JwtUtil;
import com.example.foodshop.identity.service.TokenBlacklistService;
import com.example.foodshop.identity.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(request);
            String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());
            
            AuthResponse response = new AuthResponse(token, user.getId(), user.getUsername(), 
                                                    user.getEmail(), user.getRole());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = userService.findByUsername(request.getUsername());
            String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());

            AuthResponse response = new AuthResponse(token, user.getId(), user.getUsername(), 
                                                    user.getEmail(), user.getRole());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(token);
        }
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token) && !tokenBlacklistService.isBlacklisted(token)) {
                return ResponseEntity.ok("Token is valid");
            }
        }
        return ResponseEntity.status(401).body("Invalid token");
    }
}
