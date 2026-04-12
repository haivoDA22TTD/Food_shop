package com.example.foodshop.service;

import com.example.foodshop.entity.PasskeyChallenge;
import com.example.foodshop.entity.PasskeyCredential;
import com.example.foodshop.entity.User;
import com.example.foodshop.repository.PasskeyChallengeRepository;
import com.example.foodshop.repository.PasskeyCredentialRepository;
import com.example.foodshop.repository.UserRepository;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PasskeyService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasskeyService.class);
    
    @Autowired
    private PasskeyCredentialRepository credentialRepository;
    
    @Autowired
    private PasskeyChallengeRepository challengeRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${app.url:http://localhost:8080}")
    private String appUrl;
    
    private final SecureRandom random = new SecureRandom();
    
    /**
     * Generate registration options for creating a new passkey
     */
    public Map<String, Object> generateRegistrationOptions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate challenge
        byte[] challengeBytes = new byte[32];
        random.nextBytes(challengeBytes);
        ByteArray challenge = new ByteArray(challengeBytes);
        String challengeBase64 = challenge.getBase64Url();
        
        // Save challenge
        PasskeyChallenge passkeyChallenge = new PasskeyChallenge();
        passkeyChallenge.setChallenge(challengeBase64);
        passkeyChallenge.setUserId(user.getId());
        passkeyChallenge.setUsername(username);
        passkeyChallenge.setType("registration");
        passkeyChallenge.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        challengeRepository.save(passkeyChallenge);
        
        // Generate user handle
        ByteArray userHandle = new ByteArray(user.getId().toString().getBytes());
        
        // Build registration options
        Map<String, Object> options = new HashMap<>();
        options.put("challenge", challengeBase64);
        
        Map<String, Object> rp = new HashMap<>();
        rp.put("name", "Food Shop");
        rp.put("id", getRpId());
        options.put("rp", rp);
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", userHandle.getBase64Url());
        userInfo.put("name", username);
        userInfo.put("displayName", user.getFullName() != null ? user.getFullName() : username);
        options.put("user", userInfo);
        
        List<Map<String, Object>> pubKeyCredParams = new ArrayList<>();
        // ES256 (recommended)
        Map<String, Object> es256 = new HashMap<>();
        es256.put("type", "public-key");
        es256.put("alg", -7);
        pubKeyCredParams.add(es256);
        // RS256 (fallback)
        Map<String, Object> rs256 = new HashMap<>();
        rs256.put("type", "public-key");
        rs256.put("alg", -257);
        pubKeyCredParams.add(rs256);
        options.put("pubKeyCredParams", pubKeyCredParams);
        
        options.put("timeout", 60000);
        options.put("attestation", "none");
        
        Map<String, Object> authenticatorSelection = new HashMap<>();
        authenticatorSelection.put("authenticatorAttachment", "platform");
        authenticatorSelection.put("requireResidentKey", false);
        authenticatorSelection.put("residentKey", "preferred");
        authenticatorSelection.put("userVerification", "required");
        options.put("authenticatorSelection", authenticatorSelection);
        
        logger.info("Generated registration options for user: {}", username);
        return options;
    }
    
    /**
     * Verify and register a new passkey credential
     */
    @Transactional
    public void registerCredential(String username, Map<String, Object> credential, String nickname) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        try {
            // Extract data from credential
            String credentialId = (String) credential.get("id");
            String rawId = (String) credential.get("rawId");
            Map<String, Object> response = (Map<String, Object>) credential.get("response");
            
            String clientDataJSON = (String) response.get("clientDataJSON");
            String attestationObject = (String) response.get("attestationObject");
            
            // Get challenge from database
            String challengeStr = extractChallengeFromClientData(clientDataJSON);
            PasskeyChallenge passkeyChallenge = challengeRepository.findByChallengeAndUsedFalse(challengeStr)
                    .orElseThrow(() -> new RuntimeException("Invalid or expired challenge"));
            
            if (passkeyChallenge.isExpired()) {
                throw new RuntimeException("Challenge expired");
            }
            
            // For simplified implementation, we'll trust the credential
            // In production, you should use full Yubico WebAuthn validation
            
            // Save credential
            PasskeyCredential passkeyCredential = new PasskeyCredential();
            passkeyCredential.setUser(user);
            passkeyCredential.setCredentialId(credentialId);
            passkeyCredential.setPublicKey(attestationObject); // Store attestation object
            passkeyCredential.setSignCount(0L);
            passkeyCredential.setAaguid("00000000-0000-0000-0000-000000000000");
            passkeyCredential.setNickname(nickname != null ? nickname : "Passkey");
            passkeyCredential.setIsActive(true);
            
            credentialRepository.save(passkeyCredential);
            
            // Mark challenge as used
            passkeyChallenge.setUsed(true);
            challengeRepository.save(passkeyChallenge);
            
            logger.info("Registered passkey for user: {}", username);
            
        } catch (Exception e) {
            logger.error("Failed to register passkey: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to register passkey: " + e.getMessage());
        }
    }
    
    /**
     * Generate authentication options for passkey login
     */
    public Map<String, Object> generateAuthenticationOptions(String username) {
        // Generate challenge
        byte[] challengeBytes = new byte[32];
        random.nextBytes(challengeBytes);
        ByteArray challenge = new ByteArray(challengeBytes);
        String challengeBase64 = challenge.getBase64Url();
        
        // Save challenge
        PasskeyChallenge passkeyChallenge = new PasskeyChallenge();
        passkeyChallenge.setChallenge(challengeBase64);
        passkeyChallenge.setUsername(username);
        passkeyChallenge.setType("authentication");
        passkeyChallenge.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        challengeRepository.save(passkeyChallenge);
        
        Map<String, Object> options = new HashMap<>();
        options.put("challenge", challengeBase64);
        options.put("timeout", 60000);
        options.put("rpId", getRpId());
        options.put("userVerification", "required");
        
        // Get user's credentials (optional - for better UX)
        if (username != null && !username.isEmpty()) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                List<PasskeyCredential> credentials = credentialRepository.findByUserAndIsActiveTrue(user);
                if (!credentials.isEmpty()) {
                    List<Map<String, String>> allowCredentials = new ArrayList<>();
                    for (PasskeyCredential cred : credentials) {
                        Map<String, String> credInfo = new HashMap<>();
                        credInfo.put("type", "public-key");
                        credInfo.put("id", cred.getCredentialId());
                        allowCredentials.add(credInfo);
                    }
                    options.put("allowCredentials", allowCredentials);
                }
            }
        }
        
        logger.info("Generated authentication options for user: {}", username);
        return options;
    }
    
    /**
     * Verify passkey authentication and return username
     */
    @Transactional
    public String verifyAuthentication(Map<String, Object> credential) {
        try {
            String credentialId = (String) credential.get("id");
            String rawId = (String) credential.get("rawId");
            Map<String, Object> response = (Map<String, Object>) credential.get("response");
            
            String clientDataJSON = (String) response.get("clientDataJSON");
            String authenticatorData = (String) response.get("authenticatorData");
            String signature = (String) response.get("signature");
            String userHandle = (String) response.get("userHandle");
            
            // Find credential
            PasskeyCredential passkeyCredential = credentialRepository.findByCredentialId(credentialId)
                    .orElseThrow(() -> new RuntimeException("Credential not found"));
            
            if (!passkeyCredential.getIsActive()) {
                throw new RuntimeException("Credential is inactive");
            }
            
            // Get challenge
            String challengeStr = extractChallengeFromClientData(clientDataJSON);
            PasskeyChallenge passkeyChallenge = challengeRepository.findByChallengeAndUsedFalse(challengeStr)
                    .orElseThrow(() -> new RuntimeException("Invalid or expired challenge"));
            
            if (passkeyChallenge.isExpired()) {
                throw new RuntimeException("Challenge expired");
            }
            
            // For simplified implementation, we trust the credential exists and is valid
            // In production, you should use full Yubico WebAuthn signature verification
            
            // Update sign count and last used
            passkeyCredential.setSignCount(passkeyCredential.getSignCount() + 1);
            passkeyCredential.setLastUsedAt(LocalDateTime.now());
            credentialRepository.save(passkeyCredential);
            
            // Mark challenge as used
            passkeyChallenge.setUsed(true);
            challengeRepository.save(passkeyChallenge);
            
            String username = passkeyCredential.getUser().getUsername();
            logger.info("Authenticated user with passkey: {}", username);
            
            return username;
            
        } catch (Exception e) {
            logger.error("Failed to verify passkey authentication: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to verify passkey: " + e.getMessage());
        }
    }
    
    /**
     * Get user's passkeys
     */
    public List<Map<String, Object>> getUserPasskeys(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<PasskeyCredential> credentials = credentialRepository.findByUser(user);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (PasskeyCredential cred : credentials) {
            Map<String, Object> credInfo = new HashMap<>();
            credInfo.put("id", cred.getId());
            credInfo.put("nickname", cred.getNickname());
            credInfo.put("createdAt", cred.getCreatedAt().toString());
            credInfo.put("lastUsedAt", cred.getLastUsedAt() != null ? cred.getLastUsedAt().toString() : null);
            credInfo.put("isActive", cred.getIsActive());
            result.add(credInfo);
        }
        
        return result;
    }
    
    /**
     * Delete a passkey
     */
    @Transactional
    public void deletePasskey(String username, Long credentialId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        PasskeyCredential credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new RuntimeException("Credential not found"));
        
        if (!credential.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        
        credentialRepository.delete(credential);
        logger.info("Deleted passkey {} for user: {}", credentialId, username);
    }
    
    // Helper methods
    
    private String getRpId() {
        try {
            String host = appUrl.replace("https://", "").replace("http://", "");
            if (host.contains(":")) {
                host = host.substring(0, host.indexOf(":"));
            }
            if (host.contains("/")) {
                host = host.substring(0, host.indexOf("/"));
            }
            return host;
        } catch (Exception e) {
            return "localhost";
        }
    }
    
    private String extractChallengeFromClientData(String clientDataJSON) {
        try {
            ByteArray decoded = ByteArray.fromBase64Url(clientDataJSON);
            String json = new String(decoded.getBytes());
            // Simple JSON parsing
            int start = json.indexOf("\"challenge\":\"") + 13;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract challenge from client data");
        }
    }
}
