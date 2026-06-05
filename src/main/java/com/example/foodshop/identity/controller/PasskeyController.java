package com.example.foodshop.identity.controller;

import com.example.foodshop.identity.entity.PasskeyCredential;
import com.example.foodshop.identity.entity.User;
import com.example.foodshop.identity.repository.UserRepository;
import com.example.foodshop.identity.security.JwtUtil;
import com.example.foodshop.identity.service.PasskeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PasskeyController - WebAuthn/Passkey authentication endpoints
 */
@RestController
@RequestMapping("/api/auth/passkey")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Passkey Authentication", description = "WebAuthn/Passkey authentication endpoints")
public class PasskeyController {

    private final PasskeyService passkeyService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    /**
     * Generate registration options for creating a new passkey
     */
    @PostMapping("/register/options")
    @Operation(summary = "Generate passkey registration options")
    public ResponseEntity<?> getRegistrationOptions(Authentication authentication) {
        try {
            String username = authentication.getName();
            log.info("=== PASSKEY REGISTRATION DEBUG ===");
            log.info("Attempting to find user by username: {}", username);
            
            // Find user by username (not email) because JWT sub claim contains username
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

            log.info("User found: id={}, email={}", user.getId(), user.getEmail());
            
            String optionsJson = passkeyService.generateRegistrationOptions(user.getId());
            
            // Return as raw JSON string with proper content type
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(optionsJson);
        } catch (Exception e) {
            log.error("Error generating registration options", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Verify and save passkey credential after registration
     */
    @PostMapping("/register/verify")
    @Operation(summary = "Verify and save passkey credential")
    public ResponseEntity<Map<String, Object>> verifyRegistration(
            Authentication authentication,
            @RequestBody Map<String, Object> request) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            String credentialJson = (String) request.get("credential");
            String nickname = (String) request.get("nickname");

            passkeyService.verifyRegistration(user.getId(), credentialJson, nickname);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Passkey registered successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error verifying registration", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Generate authentication options for passkey login
     */
    @PostMapping("/login/options")
    @Operation(summary = "Generate passkey authentication options")
    public ResponseEntity<?> getAuthenticationOptions(@RequestBody Map<String, String> request) {
        try {
            String identifier = request.get("email"); // Can be username or email
            log.info("=== PASSKEY LOGIN OPTIONS DEBUG ===");
            log.info("Received identifier: {}", identifier);
            
            String optionsJson = passkeyService.generateAuthenticationOptions(identifier);
            
            log.info("Generated authentication options successfully");
            
            // Return as raw JSON string with proper content type
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(optionsJson);
        } catch (Exception e) {
            log.error("Error generating authentication options", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Verify passkey authentication and return JWT token
     */
    @PostMapping("/login/verify")
    @Operation(summary = "Verify passkey authentication")
    public ResponseEntity<Map<String, Object>> verifyAuthentication(@RequestBody Map<String, Object> request) {
        try {
            log.info("=== PASSKEY LOGIN VERIFY DEBUG ===");
            String assertionJson = (String) request.get("assertion");
            log.info("Received assertion length: {}", assertionJson != null ? assertionJson.length() : 0);
            
            User user = passkeyService.verifyAuthentication(assertionJson);

            log.info("Authentication successful for user: email={}, id={}", user.getEmail(), user.getId());

            // Generate JWT token with username (not email) as the sub claim
            // This matches the pattern used in regular login
            String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole()
            ));
            
            log.info("Returning JWT token for user: {}", user.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error verifying authentication", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get user's registered passkeys
     */
    @GetMapping("/list")
    @Operation(summary = "Get user's passkeys")
    public ResponseEntity<List<PasskeyCredential>> getUserPasskeys(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<PasskeyCredential> passkeys = passkeyService.getUserPasskeys(user.getId());
            
            return ResponseEntity.ok(passkeys);
        } catch (Exception e) {
            log.error("Error getting user passkeys", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete a passkey
     */
    @DeleteMapping("/{credentialId}")
    @Operation(summary = "Delete a passkey")
    public ResponseEntity<Map<String, Object>> deletePasskey(
            Authentication authentication,
            @PathVariable Long credentialId) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            passkeyService.deletePasskey(user.getId(), credentialId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Passkey deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting passkey", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Debug endpoint: Clean up expired challenges (should be called by scheduled job in production)
     */
    @PostMapping("/debug/cleanup-challenges")
    @Operation(summary = "Clean up expired challenges")
    public ResponseEntity<Map<String, Object>> cleanupChallenges() {
        try {
            passkeyService.cleanupExpiredChallenges();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Expired challenges cleaned up");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error cleaning up challenges", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
