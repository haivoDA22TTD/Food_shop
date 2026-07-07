package com.example.foodshop.product.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class RateLimitService {
    private final Map<String, Deque<Instant>> buckets = new ConcurrentHashMap<>();

    public boolean allow(String key, int maxRequests, Duration window) {
        Instant now = Instant.now();
        Instant cutoff = now.minus(window);

        Deque<Instant> deque = buckets.computeIfAbsent(key, ignored -> new ConcurrentLinkedDeque<>());
        while (true) {
            Instant first = deque.peekFirst();
            if (first == null || !first.isBefore(cutoff)) {
                break;
            }
            deque.pollFirst();
        }

        if (deque.size() >= maxRequests) {
            return false;
        }

        deque.addLast(now);
        return true;
    }
}
