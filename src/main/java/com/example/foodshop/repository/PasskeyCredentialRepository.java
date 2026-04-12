package com.example.foodshop.repository;

import com.example.foodshop.entity.PasskeyCredential;
import com.example.foodshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasskeyCredentialRepository extends JpaRepository<PasskeyCredential, Long> {
    Optional<PasskeyCredential> findByCredentialId(String credentialId);
    List<PasskeyCredential> findByUser(User user);
    List<PasskeyCredential> findByUserAndIsActiveTrue(User user);
}
