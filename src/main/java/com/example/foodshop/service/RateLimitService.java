package com.example.foodshop.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.redis.jedis.cas.JedisBasedProxyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.function.Supplier;

@Service
public class RateLimitService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);

    private final JedisBasedProxyManager<byte[]> proxyManager;
    private final boolean redisAvailable;

    @Autowired
    public RateLimitService(JedisConnectionFactory jedisConnectionFactory) {
        JedisBasedProxyManager<byte[]> tempProxyManager = null;
        boolean available = false;

        try {
            // Get Jedis pool from connection factory
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(8);
            poolConfig.setMaxIdle(8);
            poolConfig.setMinIdle(0);

            // Extract connection details
            String host = jedisConnectionFactory.getHostName();
            int port = jedisConnectionFactory.getPort();
            String password = jedisConnectionFactory.getPassword();

            JedisPool jedisPool;
            if (password != null && !password.isEmpty()) {
                jedisPool = new JedisPool(poolConfig, host, port, 2000, password);
            } else {
                jedisPool = new JedisPool(poolConfig, host, port, 2000);
            }

            // Test connection
            try (var jedis = jedisPool.getResource()) {
                jedis.ping();
                logger.info("✅ Rate limiting Redis connection successful");
            }

            tempProxyManager = JedisBasedProxyManager.builderFor(jedisPool)
                    .build();
            available = true;

        } catch (Exception e) {
            logger.warn("⚠️ Redis not available for rate limiting: {}", e.getMessage());
            logger.warn("⚠️ Rate limiting will be disabled (fail-open)");
        }

        this.proxyManager = tempProxyManager;
        this.redisAvailable = available;
    }

    /**
     * Check if request is allowed based on rate limit
     * 
     * @param key Unique key for rate limiting (e.g., "login:192.168.1.1" or "chatbot:user123")
     * @param capacity Maximum requests allowed
     * @param refillTokens Number of tokens to refill
     * @param refillDuration Duration for refill
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String key, long capacity, long refillTokens, Duration refillDuration) {
        if (!redisAvailable) {
            // Fail-open: allow all requests if Redis is down
            logger.debug("Redis unavailable, allowing request (fail-open)");
            return true;
        }

        try {
            Supplier<BucketConfiguration> configSupplier = () -> {
                Bandwidth limit = Bandwidth.classic(capacity, Refill.intervally(refillTokens, refillDuration));
                return BucketConfiguration.builder()
                        .addLimit(limit)
                        .build();
            };

            // Convert String key to byte[] for Bucket4j
            byte[] keyBytes = key.getBytes();
            Bucket bucket = proxyManager.builder().build(keyBytes, configSupplier);
            boolean allowed = bucket.tryConsume(1);

            if (!allowed) {
                logger.warn("⚠️ Rate limit exceeded for key: {}", key);
            }

            return allowed;

        } catch (Exception e) {
            logger.error("Rate limit check failed: {}", e.getMessage());
            // Fail-open: allow request if error
            return true;
        }
    }

    /**
     * Login rate limit: 5 attempts per minute per IP
     */
    public boolean isLoginAllowed(String ipAddress) {
        return isAllowed(
                "login:" + ipAddress,
                5,  // 5 requests
                5,  // refill 5 tokens
                Duration.ofMinutes(1)  // per minute
        );
    }

    /**
     * Register rate limit: 3 registrations per hour per IP
     */
    public boolean isRegisterAllowed(String ipAddress) {
        return isAllowed(
                "register:" + ipAddress,
                3,  // 3 requests
                3,  // refill 3 tokens
                Duration.ofHours(1)  // per hour
        );
    }

    /**
     * Chatbot rate limit: 20 messages per minute per user
     */
    public boolean isChatbotAllowed(String userId) {
        return isAllowed(
                "chatbot:" + userId,
                20,  // 20 requests
                20,  // refill 20 tokens
                Duration.ofMinutes(1)  // per minute
        );
    }

    /**
     * Order creation rate limit: 10 orders per hour per user
     */
    public boolean isOrderAllowed(String userId) {
        return isAllowed(
                "order:" + userId,
                10,  // 10 requests
                10,  // refill 10 tokens
                Duration.ofHours(1)  // per hour
        );
    }

    /**
     * Review submission rate limit: 5 reviews per hour per user
     */
    public boolean isReviewAllowed(String userId) {
        return isAllowed(
                "review:" + userId,
                5,  // 5 requests
                5,  // refill 5 tokens
                Duration.ofHours(1)  // per hour
        );
    }

    /**
     * Generic API rate limit: 100 requests per minute per IP
     */
    public boolean isApiAllowed(String ipAddress) {
        return isAllowed(
                "api:" + ipAddress,
                100,  // 100 requests
                100,  // refill 100 tokens
                Duration.ofMinutes(1)  // per minute
        );
    }

    /**
     * Check if Redis is available for rate limiting
     */
    public boolean isRedisAvailable() {
        return redisAvailable;
    }
}
