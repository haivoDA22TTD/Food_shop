package com.example.foodshop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    
    // In-memory fallback when Redis is unavailable
    private final Map<String, Long> inMemoryBlacklist = new ConcurrentHashMap<>();
    private boolean redisAvailable = true;

    /**
     * Add token to blacklist
     * @param token JWT token to blacklist
     * @param expirationTimeInSeconds Time until token expires naturally
     */
    public void blacklistToken(String token, long expirationTimeInSeconds) {
        boolean success = false;
        
        // Try Redis first
        if (redisTemplate != null) {
            try {
                String key = BLACKLIST_PREFIX + token;
                redisTemplate.opsForValue().set(key, "blacklisted", expirationTimeInSeconds, TimeUnit.SECONDS);
                logger.info("✅ Token blacklisted in Redis. TTL: {} seconds", expirationTimeInSeconds);
                redisAvailable = true;
                success = true;
            } catch (Exception e) {
                logger.error("❌ Failed to blacklist token in Redis: {}", e.getMessage());
                redisAvailable = false;
            }
        }
        
        // Fallback to in-memory if Redis failed or unavailable
        if (!success) {
            long expirationTime = System.currentTimeMillis() + (expirationTimeInSeconds * 1000);
            inMemoryBlacklist.put(token, expirationTime);
            logger.warn("⚠️ Token blacklisted in MEMORY (Redis unavailable). TTL: {} seconds", expirationTimeInSeconds);
            
            // Clean up expired tokens from memory
            cleanupExpiredTokens();
        }
    }

    /**
     * Check if token is blacklisted
     * @param token JWT token to check
     * @return true if token is blacklisted, false otherwise
     */
    public boolean isTokenBlacklisted(String token) {
        // Check Redis first
        if (redisTemplate != null && redisAvailable) {
            try {
                String key = BLACKLIST_PREFIX + token;
                Boolean exists = redisTemplate.hasKey(key);
                boolean isBlacklisted = exists != null && exists;
                
                if (isBlacklisted) {
                    logger.debug("🚫 Token is blacklisted (Redis)");
                }
                
                return isBlacklisted;
            } catch (Exception e) {
                logger.error("❌ Failed to check Redis blacklist: {}", e.getMessage());
                redisAvailable = false;
                // Fall through to in-memory check
            }
        }
        
        // Check in-memory fallback
        Long expirationTime = inMemoryBlacklist.get(token);
        if (expirationTime != null) {
            if (System.currentTimeMillis() < expirationTime) {
                logger.debug("🚫 Token is blacklisted (Memory)");
                return true;
            } else {
                // Token expired, remove from memory
                inMemoryBlacklist.remove(token);
            }
        }
        
        return false;
    }

    /**
     * Remove token from blacklist (if needed for testing)
     * @param token JWT token to remove
     */
    public void removeFromBlacklist(String token) {
        // Remove from Redis
        if (redisTemplate != null) {
            try {
                String key = BLACKLIST_PREFIX + token;
                redisTemplate.delete(key);
                logger.info("Token removed from Redis blacklist");
            } catch (Exception e) {
                logger.error("Failed to remove token from Redis: {}", e.getMessage());
            }
        }
        
        // Remove from memory
        inMemoryBlacklist.remove(token);
        logger.info("Token removed from memory blacklist");
    }

    /**
     * Get remaining TTL for blacklisted token
     * @param token JWT token
     * @return TTL in seconds, -1 if key doesn't exist or Redis is down
     */
    public long getTokenTTL(String token) {
        // Try Redis first
        if (redisTemplate != null && redisAvailable) {
            try {
                String key = BLACKLIST_PREFIX + token;
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                return ttl != null ? ttl : -1;
            } catch (Exception e) {
                logger.error("Failed to get token TTL from Redis: {}", e.getMessage());
                redisAvailable = false;
            }
        }
        
        // Check in-memory
        Long expirationTime = inMemoryBlacklist.get(token);
        if (expirationTime != null) {
            long remaining = (expirationTime - System.currentTimeMillis()) / 1000;
            return remaining > 0 ? remaining : -1;
        }
        
        return -1;
    }
    
    /**
     * Clean up expired tokens from in-memory blacklist
     */
    private void cleanupExpiredTokens() {
        long now = System.currentTimeMillis();
        inMemoryBlacklist.entrySet().removeIf(entry -> entry.getValue() < now);
    }
    
    /**
     * Get blacklist status for monitoring
     */
    public Map<String, Object> getStatus() {
        return Map.of(
            "redisAvailable", redisAvailable,
            "inMemorySize", inMemoryBlacklist.size(),
            "usingFallback", !redisAvailable || redisTemplate == null
        );
    }
}
