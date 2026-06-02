package com.example.foodshop.identity.controller;

import com.example.foodshop.identity.dto.AuthResponse;
import com.example.foodshop.identity.entity.PasskeyCredential;
import com.example.foodshop.identity.entity.User;
import com.example.foodshop.identity.security.JwtUtil;
import com.example.foodshop.identity.service.PasskeyService;
import com.example.foodshop.identity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/passkey")
public class PasskeyController {

    @Autowired
    private PasskeyService passkeyService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register/start")
    public ResponseEntity<?> startRegistration(@RequestBody Map<String, Object> request, 
                                               Authentication authentication) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String username = authentication.getName();
            
            String challenge = passkeyService.startRegistration(userId, username);
            return ResponseEntity.ok(Map.of("challenge", challenge));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register/finish")
    public ResponseEntity<?> finishRegistration(@RequestBody Map<String, Object> request,
                                                Authentication authentication) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String credentialId = request.get("credentialId").toString();
            String publicKey = request.get("publicKey").toString();
            String nickname = request.get("nickname").toString();
            
            passkeyService.finishRegistration(userId, credentialId, publicKey, nickname);
            return ResponseEntity.ok("Passkey registered successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/authenticate/start")
    public ResponseEntity<?> startAuthentication(@RequestBody Map<String, Object> request) {
        try {
            String username = request.get("username").toString();
            String challenge = passkeyService.startAuthentication(username);
            return ResponseEntity.ok(Map.of(
                "challenge", challenge,
                "username", username
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/authenticate/finish")
    public ResponseEntity<?> finishAuthentication(@RequestBody Map<String, Object> request) {
        try {
            String credentialId = request.get("credentialId").toString();
            String signature = request.get("signature").toString();
            String authenticatorData = request.get("authenticatorData").toString();
            String clientDataJSON = request.get("clientDataJSON").toString();
            
            Map<String, Object> authResult = passkeyService.finishAuthentication(
                credentialId, signature, authenticatorData, clientDataJSON
            );
            
            // Get user info and generate JWT
            Long userId = Long.parseLong(authResult.get("userId").toString());
            User user = userService.findById(userId);
            
            String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());
            
            AuthResponse response = new AuthResponse(
                token, user.getId(), user.getUsername(), user.getEmail(), user.getRole()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listCredentials(@RequestParam Long userId, Authentication authentication) {
        try {
            List<PasskeyCredential> credentials = passkeyService.getUserCredentials(userId);
            return ResponseEntity.ok(credentials);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{credentialId}")
    public ResponseEntity<?> deleteCredential(@PathVariable Long credentialId,
                                              @RequestParam Long userId,
                                              Authentication authentication) {
        try {
            passkeyService.deleteCredential(credentialId, userId);
            return ResponseEntity.ok("Passkey deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
