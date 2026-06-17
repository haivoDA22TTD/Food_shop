package com.example.foodshop.identity.repository;

import com.example.foodshop.identity.entity.PasskeyCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasskeyCredentialRepository extends JpaRepository<PasskeyCredential, Long> {
    
    Optional<PasskeyCredential> findByCredentialId(String credentialId);
    
    Optional<PasskeyCredential> findByCredentialIdAndIsActive(String credentialId, Boolean isActive);
    
    List<PasskeyCredential> findByUserId(Long userId);
    
    List<PasskeyCredential> findByUserIdAndIsActive(Long userId, Boolean isActive);
}
