package com.example.foodshop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    /**
     * Add token to blacklist
     * @param token JWT token to blacklist
     * @param expirationTimeInSeconds Time until token expires naturally
     */
    public void blacklistToken(String token, long expirationTimeInSeconds) {
        try {
            String key = BLACKLIST_PREFIX + token;
            // Store token in Redis with expiration time
            // Value doesn't matter, we just check if key exists
            redisTemplate.opsForValue().set(key, "blacklisted", expirationTimeInSeconds, TimeUnit.SECONDS);
            logger.info("Token blacklisted successfully. TTL: {} seconds", expirationTimeInSeconds);
        } catch (Exception e) {
            logger.error("Failed to blacklist token: {}", e.getMessage());
            // Don't throw exception, just log error
            // Application should continue even if Redis is down
        }
    }

    /**
     * Check if token is blacklisted
     * @param token JWT token to check
     * @return true if token is blacklisted, false otherwise (or if Redis is down)
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            Boolean exists = redisTemplate.hasKey(key);
            boolean isBlacklisted = exists != null && exists;
            
            if (isBlacklisted) {
                logger.debug("Token is blacklisted");
            }
            
            return isBlacklisted;
        } catch (Exception e) {
            logger.error("Failed to check token blacklist: {}", e.getMessage());
            // If Redis is down, allow the request (fail open)
            // This prevents Redis downtime from blocking all requests
            logger.warn("Redis unavailable, allowing request (fail-open policy)");
            return false;
        }
    }

    /**
     * Remove token from blacklist (if needed for testing)
     * @param token JWT token to remove
     */
    public void removeFromBlacklist(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.delete(key);
            logger.info("Token removed from blacklist");
        } catch (Exception e) {
            logger.error("Failed to remove token from blacklist: {}", e.getMessage());
        }
    }

    /**
     * Get remaining TTL for blacklisted token
     * @param token JWT token
     * @return TTL in seconds, -1 if key doesn't exist or Redis is down
     */
    public long getTokenTTL(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null ? ttl : -1;
        } catch (Exception e) {
            logger.error("Failed to get token TTL: {}", e.getMessage());
            return -1;
        }
    }
}
