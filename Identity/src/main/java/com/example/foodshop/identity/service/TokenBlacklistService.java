package com.example.foodshop.identity.service;

import com.example.foodshop.identity.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {
    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String BLACKLIST_PREFIX = "blacklist:";

    public void blacklistToken(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            long ttlMillis = resolveTokenTtlMillis(token);

            if (ttlMillis <= 0) {
                // Token is already expired, nothing needs to be persisted.
                log.info("Token already expired, skipping blacklist");
                return;
            }

            redisTemplate.opsForValue().set(key, "blacklisted", ttlMillis, TimeUnit.MILLISECONDS);
            log.info("Token blacklisted successfully with TTL: {} ms", ttlMillis);
        } catch (Exception e) {
            log.error("Failed to blacklist token: {}", e.getMessage(), e);
            // Re-throw to let controller handle it
            throw new IllegalStateException("Unable to persist logout token state: " + e.getMessage(), e);
        }
    }

    public boolean isBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Failed to check token blacklist state", e);
            // If Redis is unavailable, do not block all requests; fallback to JWT expiration.
            return false;
        }
    }

    private long resolveTokenTtlMillis(String token) {
        try {
            Date expiry = jwtUtil.extractExpiration(token);
            if (expiry == null) {
                log.warn("Token has no expiration date, using default TTL");
                return TimeUnit.HOURS.toMillis(24); // Default 24 hours
            }
            long ttl = Duration.between(new Date().toInstant(), expiry.toInstant()).toMillis();
            log.debug("Token TTL calculated: {} ms", ttl);
            return ttl;
        } catch (Exception e) {
            log.error("Failed to extract token expiration: {}", e.getMessage());
            // Return default TTL if extraction fails
            return TimeUnit.HOURS.toMillis(24);
        }
    }
}
