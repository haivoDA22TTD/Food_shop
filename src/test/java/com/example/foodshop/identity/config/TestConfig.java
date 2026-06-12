package com.example.foodshop.identity.config;

import com.example.foodshop.identity.service.TokenBlacklistService;
import com.example.foodshop.identity.service.AuthRateLimitService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

/**
 * Test configuration to mock Redis-dependent services
 */
@TestConfiguration
@Profile("test")
public class TestConfig {

    /**
     * Mock TokenBlacklistService for tests (no Redis needed)
     */
    @Bean
    @Primary
    public TokenBlacklistService tokenBlacklistService() {
        return new TokenBlacklistService(null, null) {
            @Override
            public void blacklistToken(String token) {
                // No-op for tests
            }

            @Override
            public boolean isBlacklisted(String token) {
                return false; // Never blacklisted in tests
            }
        };
    }

    /**
     * Mock AuthRateLimitService for tests (no Redis needed)
     */
    @Bean
    @Primary
    public AuthRateLimitService authRateLimitService() {
        return new AuthRateLimitService(null) {
            @Override
            public boolean allow(String key, int limit, Duration window) {
                return true; // Always allow in tests
            }
        };
    }
}
