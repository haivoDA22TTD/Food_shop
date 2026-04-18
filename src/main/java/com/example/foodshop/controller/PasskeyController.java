package com.example.foodshop.controller;

import com.example.foodshop.security.JwtUtil;
import com.example.foodshop.service.CustomUserDetailsService;
import com.example.foodshop.service.PasskeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/passkey")
@RequiredArgsConstructor
public class PasskeyController {
    
    private final PasskeyService passkeyService;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    
    /**
     * Get registration options for creating a new passkey
     * Requires authentication
     */
    @PostMapping("/register/options")
    public ResponseEntity<?> getRegistrationOptions() {
        try {
            String username = getCurrentUsername();
            if (username == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            Map<String, Object> options = passkeyService.generateRegistrationOptions(username);
            return ResponseEntity.ok(options);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Verify and register a new passkey credential
     * Requires authentication
     */
    @PostMapping("/register/verify")
    public ResponseEntity<?> verifyRegistration(@RequestBody Map<String, Object> request) {
        try {
            String username = getCurrentUsername();
            if (username == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            Map<String, Object> credential = (Map<String, Object>) request.get("credential");
            String nickname = (String) request.get("nickname");
            
            passkeyService.registerCredential(username, credential, nickname);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Passkey đã được đăng ký thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get authentication options for passkey login
     * No authentication required
     */
    @PostMapping("/login/options")
    public ResponseEntity<?> getAuthenticationOptions(@RequestBody(required = false) Map<String, String> request) {
        try {
            String username = request != null ? request.get("username") : null;
            Map<String, Object> options = passkeyService.generateAuthenticationOptions(username);
            return ResponseEntity.ok(options);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Verify passkey authentication and return JWT token
     * No authentication required
     */
    @PostMapping("/login/verify")
    public ResponseEntity<?> verifyAuthentication(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> credential = (Map<String, Object>) request.get("credential");
            
            String username = passkeyService.verifyAuthentication(credential);
            
            // Generate JWT token
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String token = jwtUtil.generateToken(userDetails);
            String role = userDetails.getAuthorities().iterator().next().getAuthority();
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", username);
            response.put("role", role);
            response.put("message", "Đăng nhập thành công với Passkey");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get list of user's passkeys
     * Requires authentication
     */
    @GetMapping("/list")
    public ResponseEntity<?> listPasskeys() {
        try {
            String username = getCurrentUsername();
            if (username == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            List<Map<String, Object>> passkeys = passkeyService.getUserPasskeys(username);
            return ResponseEntity.ok(passkeys);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Delete a passkey
     * Requires authentication
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePasskey(@PathVariable Long id) {
        try {
            String username = getCurrentUsername();
            if (username == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            passkeyService.deletePasskey(username, id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Passkey đã được xóa thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Helper method to get current username from security context or cookie
    private String getCurrentUsername() {
        // Try security context first
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return null;
    }
    
    // Helper to get token from request header or cookie
    private String getTokenFromRequest(jakarta.servlet.http.HttpServletRequest request) {
        // Try Authorization header first
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // Try cookie
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("auth_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }
}
