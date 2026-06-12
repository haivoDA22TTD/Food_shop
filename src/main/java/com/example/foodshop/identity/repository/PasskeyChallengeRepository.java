package com.example.foodshop.identity.repository;

import com.example.foodshop.identity.entity.PasskeyChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasskeyChallengeRepository extends JpaRepository<PasskeyChallenge, Long> {
    
    /**
     * Find challenge by challenge string
     */
    Optional<PasskeyChallenge> findByChallenge(String challenge);
    
    /**
     * Find the most recent challenge for a user by type
     */
    Optional<PasskeyChallenge> findTopByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type);
    
    /**
     * Find all challenges for a user
     */
    List<PasskeyChallenge> findByUserId(Long userId);
    
    /**
     * Delete expired challenges
     */
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
    
    /**
     * Delete all challenges for a user
     */
    void deleteByUserId(Long userId);
}
