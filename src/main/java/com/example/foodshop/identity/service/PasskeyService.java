package com.example.foodshop.identity.service;

import com.example.foodshop.identity.entity.PasskeyChallenge;
import com.example.foodshop.identity.entity.PasskeyCredential;
import com.example.foodshop.identity.entity.User;
import com.example.foodshop.identity.repository.PasskeyChallengeRepository;
import com.example.foodshop.identity.repository.PasskeyCredentialRepository;
import com.example.foodshop.identity.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * PasskeyService - WebAuthn implementation using Yubico library v2.5.2
 */
@Service
@Slf4j
public class PasskeyService {

    private final PasskeyChallengeRepository challengeRepository;
    private final PasskeyCredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final RelyingParty relyingParty;

    private static final int CHALLENGE_EXPIRY_MINUTES = 5;

    public PasskeyService(
            PasskeyChallengeRepository challengeRepository,
            PasskeyCredentialRepository credentialRepository,
            UserRepository userRepository,
            @Value("${webauthn.rp.id:localhost}") String rpId,
            @Value("${webauthn.rp.name:Food Shop}") String rpName) {
        this.challengeRepository = challengeRepository;
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;

        // Initialize RelyingParty
        this.relyingParty = RelyingParty.builder()
                .identity(RelyingPartyIdentity.builder()
                        .id(rpId)
                        .name(rpName)
                        .build())
                .credentialRepository(new CredentialRepository() {
                    @Override
                    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
                        try {
                            User user = userRepository.findByEmail(username).orElse(null);
                            if (user == null) return Collections.emptySet();

                            List<PasskeyCredential> credentials = credentialRepository.findByUserIdAndIsActive(user.getId(), true);
                            Set<PublicKeyCredentialDescriptor> result = new HashSet<>();
                            for (PasskeyCredential cred : credentials) {
                                result.add(PublicKeyCredentialDescriptor.builder()
                                        .id(ByteArray.fromBase64Url(cred.getCredentialId()))
                                        .build());
                            }
                            return result;
                        } catch (Exception e) {
                            log.error("Error getting credential IDs", e);
                            return Collections.emptySet();
                        }
                    }

                    @Override
                    public Optional<ByteArray> getUserHandleForUsername(String username) {
                        User user = userRepository.findByEmail(username).orElse(null);
                        if (user == null) return Optional.empty();
                        return Optional.of(new ByteArray(user.getId().toString().getBytes()));
                    }

                    @Override
                    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
                        try {
                            Long userId = Long.parseLong(new String(userHandle.getBytes()));
                            User user = userRepository.findById(userId).orElse(null);
                            if (user == null) return Optional.empty();
                            return Optional.of(user.getEmail());
                        } catch (Exception e) {
                            return Optional.empty();
                        }
                    }

                    @Override
                    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
                        try {
                            String credId = credentialId.getBase64Url();
                            PasskeyCredential cred = credentialRepository.findByCredentialIdAndIsActive(credId, true).orElse(null);
                            if (cred == null) return Optional.empty();

                            return Optional.of(RegisteredCredential.builder()
                                    .credentialId(credentialId)
                                    .userHandle(userHandle)
                                    .publicKeyCose(ByteArray.fromBase64(cred.getPublicKey()))
                                    .signatureCount(cred.getSignCount())
                                    .build());
                        } catch (Exception e) {
                            log.error("Error looking up credential", e);
                            return Optional.empty();
                        }
                    }

                    @Override
                    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
                        try {
                            String credId = credentialId.getBase64Url();
                            PasskeyCredential cred = credentialRepository.findByCredentialIdAndIsActive(credId, true).orElse(null);
                            if (cred == null) return Collections.emptySet();

                            User user = userRepository.findById(cred.getUserId()).orElse(null);
                            if (user == null) return Collections.emptySet();

                            RegisteredCredential registered = RegisteredCredential.builder()
                                    .credentialId(credentialId)
                                    .userHandle(new ByteArray(user.getId().toString().getBytes()))
                                    .publicKeyCose(ByteArray.fromBase64(cred.getPublicKey()))
                                    .signatureCount(cred.getSignCount())
                                    .build();

                            return Collections.singleton(registered);
                        } catch (Exception e) {
                            log.error("Error looking up all credentials", e);
                            return Collections.emptySet();
                        }
                    }
                })
                .build();
    }

    /**
     * Generate registration options for WebAuthn
     */
    @Transactional
    public String generateRegistrationOptions(Long userId) throws JsonProcessingException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserIdentity userIdentity = UserIdentity.builder()
                .name(user.getEmail())
                .displayName(user.getEmail())
                .id(new ByteArray(userId.toString().getBytes()))
                .build();

        StartRegistrationOptions registrationOptions = StartRegistrationOptions.builder()
                .user(userIdentity)
                .build();

        PublicKeyCredentialCreationOptions creationOptions = relyingParty.startRegistration(registrationOptions);

        // Use toJson() for database storage (can be restored with fromJson())
        // toCredentialsCreateJson() is for browser only
        String requestJson = creationOptions.toJson();

        // Save challenge + full request JSON to database
        PasskeyChallenge passkeyChallenge = new PasskeyChallenge();
        passkeyChallenge.setUserId(userId);
        passkeyChallenge.setChallenge(creationOptions.getChallenge().getBase64Url());
        passkeyChallenge.setRequestJson(requestJson);
        passkeyChallenge.setType("REGISTRATION");
        passkeyChallenge.setExpiresAt(LocalDateTime.now().plusMinutes(CHALLENGE_EXPIRY_MINUTES));
        challengeRepository.save(passkeyChallenge);

        // Return toCredentialsCreateJson() for browser
        return creationOptions.toCredentialsCreateJson();
    }

    /**
     * Verify and save passkey credential after registration
     */
    @Transactional
    public void verifyRegistration(Long userId, String credentialJson, String nickname)
            throws IOException, RegistrationFailedException {

        try {
            // Find challenge - check userId != null to avoid NullPointerException
            PasskeyChallenge passkeyChallenge = challengeRepository.findAll().stream()
                    .filter(c -> c.getType().equals("REGISTRATION"))
                    .filter(c -> c.getUserId() != null && c.getUserId().equals(userId))
                    .filter(c -> c.getExpiresAt().isAfter(LocalDateTime.now()))
                    .filter(c -> {
                        // Validate that requestJson can be parsed (skip corrupt challenges)
                        try {
                            PublicKeyCredentialCreationOptions.fromJson(c.getRequestJson());
                            return true;
                        } catch (Exception e) {
                            log.warn("Skipping corrupt challenge: {}", c.getChallenge());
                            return false;
                        }
                    })
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Invalid or expired challenge"));

            // Deserialize the original creation options from DB using Yubico's fromJson()
            PublicKeyCredentialCreationOptions originalOptions =
                    PublicKeyCredentialCreationOptions.fromJson(passkeyChallenge.getRequestJson());

            PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc =
                    PublicKeyCredential.parseRegistrationResponseJson(credentialJson);

            FinishRegistrationOptions options = FinishRegistrationOptions.builder()
                    .request(originalOptions)
                    .response(pkc)
                    .build();

            RegistrationResult result = relyingParty.finishRegistration(options);

            // Save credential
            PasskeyCredential passkeyCredential = new PasskeyCredential();
            passkeyCredential.setUserId(userId);
            passkeyCredential.setCredentialId(result.getKeyId().getId().getBase64Url());
            passkeyCredential.setPublicKey(result.getPublicKeyCose().getBase64());
            passkeyCredential.setNickname(nickname);
            passkeyCredential.setSignCount(result.getSignatureCount());
            passkeyCredential.setIsActive(true);
            credentialRepository.save(passkeyCredential);

            // Delete used challenge
            challengeRepository.delete(passkeyChallenge);

            log.info("Passkey registered successfully for user: {}", userId);
        } catch (Exception e) {
            log.error("Error verifying registration", e);
            throw new RuntimeException("Failed to verify registration: " + e.getMessage(), e);
        }
    }

    /**
     * Generate authentication options for WebAuthn
     * email can be empty/null for resident key (usernameless) flow
     */
    @Transactional
    public String generateAuthenticationOptions(String email) {
        StartAssertionOptions.StartAssertionOptionsBuilder builder = StartAssertionOptions.builder();

        if (email != null && !email.trim().isEmpty()) {
            // Verify user exists when email is provided
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));
            builder.username(email);
        }
        // else: usernameless/resident key flow - browser will show all available passkeys

        AssertionRequest request = relyingParty.startAssertion(builder.build());

        // AssertionRequest.toJson() stores full request including username for server-side restoration.
        // AssertionRequest.fromJson() restores identical instance.
        // toCredentialsGetJson() is returned to the browser client.
        String requestJson;
        String credentialsGetJson;
        try {
            requestJson = request.toJson();
            credentialsGetJson = request.toCredentialsGetJson();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize AssertionRequest", e);
        }

        // Save challenge + full request JSON to database
        // For usernameless flow, we cannot save with userId - use a special marker
        Long savedUserId = null;
        if (email != null && !email.trim().isEmpty()) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                savedUserId = user.getId();
                challengeRepository.deleteByUserIdAndType(savedUserId, "AUTHENTICATION");
            }
        }
        PasskeyChallenge passkeyChallenge = new PasskeyChallenge();
        passkeyChallenge.setUserId(savedUserId);
        passkeyChallenge.setChallenge(request.getPublicKeyCredentialRequestOptions().getChallenge().getBase64Url());
        passkeyChallenge.setRequestJson(requestJson);
        passkeyChallenge.setType("AUTHENTICATION");
        passkeyChallenge.setExpiresAt(LocalDateTime.now().plusMinutes(CHALLENGE_EXPIRY_MINUTES));
        challengeRepository.save(passkeyChallenge);

        return credentialsGetJson;
    }

    /**
     * Verify passkey authentication
     */
    @Transactional
    public User verifyAuthentication(String assertionJson)
            throws IOException, AssertionFailedException {

        try {
            PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc =
                    PublicKeyCredential.parseAssertionResponseJson(assertionJson);

            String credentialId = pkc.getId().getBase64Url();
            PasskeyCredential credential = credentialRepository.findByCredentialIdAndIsActive(credentialId, true)
                    .orElseThrow(() -> new RuntimeException("Credential not found"));

            User user = userRepository.findById(credential.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Find the LATEST authentication challenge for this user
            PasskeyChallenge passkeyChallenge = challengeRepository
                    .findTopByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), "AUTHENTICATION")
                    .orElseThrow(() -> new RuntimeException("Invalid or expired challenge"));

            // Deserialize the original assertion request using Yubico's fromJson() — correct approach
            AssertionRequest originalRequest = AssertionRequest.fromJson(passkeyChallenge.getRequestJson());

            FinishAssertionOptions options = FinishAssertionOptions.builder()
                    .request(originalRequest)
                    .response(pkc)
                    .build();

            AssertionResult result = relyingParty.finishAssertion(options);

            if (!result.isSuccess()) {
                throw new RuntimeException("Authentication failed");
            }

            // Update credential sign count
            credential.setSignCount(result.getSignatureCount());
            credential.setLastUsedAt(LocalDateTime.now());
            credentialRepository.save(credential);

            // Delete used challenge
            challengeRepository.delete(passkeyChallenge);

            log.info("Passkey authentication successful for user: {}", user.getEmail());
            return user;
        } catch (Exception e) {
            log.error("Error verifying authentication", e);
            throw new RuntimeException("Failed to verify authentication: " + e.getMessage(), e);
        }
    }

    /**
     * Get user's passkeys
     */
    public List<PasskeyCredential> getUserPasskeys(Long userId) {
        return credentialRepository.findByUserIdAndIsActive(userId, true);
    }

    /**
     * Delete a passkey
     */
    @Transactional
    public void deletePasskey(Long userId, Long credentialId) {
        PasskeyCredential credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new RuntimeException("Credential not found"));

        if (!credential.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        credential.setIsActive(false);
        credentialRepository.save(credential);

        log.info("Passkey deleted for user: {}", userId);
    }

    /**
     * Clean up expired challenges
     */
    @Transactional
    public void cleanupExpiredChallenges() {
        challengeRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
    
    /**
     * Clean up all challenges for a user (useful for debugging)
     */
    @Transactional
    public void cleanupUserChallenges(Long userId) {
        challengeRepository.findAll().stream()
                .filter(c -> c.getUserId() != null && c.getUserId().equals(userId))
                .forEach(c -> challengeRepository.delete(c));
        log.info("Cleaned up all challenges for user: {}", userId);
    }
}
