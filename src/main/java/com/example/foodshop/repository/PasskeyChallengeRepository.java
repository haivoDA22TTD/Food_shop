package com.example.foodshop.repository;

import com.example.foodshop.entity.PasskeyChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasskeyChallengeRepository extends JpaRepository<PasskeyChallenge, Long> {
    
    Optional<PasskeyChallenge> findByChallengeAndUsedFalse(String challenge);
    
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
    
    void deleteByUsedTrue();
}
