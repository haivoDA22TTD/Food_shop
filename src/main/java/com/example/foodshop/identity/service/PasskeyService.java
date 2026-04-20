package com.example.foodshop.identity.service;

import com.example.foodshop.identity.entity.PasskeyChallenge;
import com.example.foodshop.identity.entity.PasskeyCredential;
import com.example.foodshop.identity.repository.PasskeyChallengeRepository;
import com.example.foodshop.identity.repository.PasskeyCredentialRepository;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class PasskeyService {

    @Autowired
    private PasskeyCredentialRepository credentialRepository;

    @Autowired
    private PasskeyChallengeRepository challengeRepository;

    private final RelyingParty relyingParty;

    public PasskeyService() {
        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
                .id("localhost")
                .name("Food Shop")
                .build();

        this.relyingParty = RelyingParty.builder()
                .identity(rpIdentity)
                .credentialRepository(new CredentialRepositoryImpl())
                .build();
    }

    @Transactional
    public String startRegistration(Long userId, String username) {
        UserIdentity userIdentity = UserIdentity.builder()
                .name(username)
                .displayName(username)
                .id(new ByteArray(userId.toString().getBytes()))
                .build();

        StartRegistrationOptions options = StartRegistrationOptions.builder()
                .user(userIdentity)
                .build();

        PublicKeyCredentialCreationOptions registration = relyingParty.startRegistration(options);
        
        String challenge = Base64.getEncoder().encodeToString(registration.getChallenge().getBytes());
        
        PasskeyChallenge passkeyChallenge = new PasskeyChallenge();
        passkeyChallenge.setUserId(userId);
        passkeyChallenge.setChallenge(challenge);
        passkeyChallenge.setType("REGISTRATION");
        passkeyChallenge.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        challengeRepository.save(passkeyChallenge);

        return challenge;
    }

    @Transactional
    public void finishRegistration(Long userId, String credentialId, String publicKey, String nickname) {
        PasskeyCredential credential = new PasskeyCredential();
        credential.setUserId(userId);
        credential.setCredentialId(credentialId);
        credential.setPublicKey(publicKey);
        credential.setNickname(nickname);
        credential.setSignCount(0L);
        credential.setIsActive(true);
        
        credentialRepository.save(credential);
    }

    public List<PasskeyCredential> getUserCredentials(Long userId) {
        return credentialRepository.findByUserIdAndIsActive(userId, true);
    }

    @Transactional
    public void deleteCredential(Long credentialId, Long userId) {
        PasskeyCredential credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new RuntimeException("Credential not found"));
        
        if (!credential.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        credential.setIsActive(false);
        credentialRepository.save(credential);
    }

    // Simple credential repository implementation
    private class CredentialRepositoryImpl implements CredentialRepository {
        @Override
        public java.util.Set<com.yubico.webauthn.data.PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
            return java.util.Collections.emptySet();
        }

        @Override
        public Optional<ByteArray> getUserHandleForUsername(String username) {
            return Optional.empty();
        }

        @Override
        public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
            return Optional.empty();
        }

        @Override
        public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
            return Optional.empty();
        }

        @Override
        public java.util.Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
            return java.util.Collections.emptySet();
        }
    }
}
