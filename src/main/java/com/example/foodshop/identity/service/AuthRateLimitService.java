package com.example.foodshop.identity.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AuthRateLimitService {

    private static class CounterWindow {
        private final AtomicInteger counter = new AtomicInteger(0);
        private volatile long windowStartMs;

        private CounterWindow(long nowMs) {
            this.windowStartMs = nowMs;
        }
    }

    private final Map<String, CounterWindow> windows = new ConcurrentHashMap<>();

    public boolean allow(String key, int maxRequests, Duration window) {
        long now = System.currentTimeMillis();
        CounterWindow counterWindow = windows.computeIfAbsent(key, ignored -> new CounterWindow(now));

        synchronized (counterWindow) {
            long elapsed = now - counterWindow.windowStartMs;
            if (elapsed >= window.toMillis()) {
                counterWindow.windowStartMs = now;
                counterWindow.counter.set(0);
            }

            int current = counterWindow.counter.incrementAndGet();
            return current <= maxRequests;
        }
    }
}
