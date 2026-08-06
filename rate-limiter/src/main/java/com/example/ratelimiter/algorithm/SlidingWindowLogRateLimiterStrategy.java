package com.example.ratelimiter.algorithm;

import com.example.ratelimiter.config.RateLimitConfig;
import com.example.ratelimiter.config.RateLimiterAlgorithm;
import com.example.ratelimiter.config.SlidingWindowLogConfig;
import com.example.ratelimiter.core.RateLimitResult;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Sliding-window-log: every admitted request's timestamp is recorded per
 * key; a new request is admitted only if fewer than {@code limit} timestamps
 * fall within the trailing {@code windowSize} interval. The most accurate of
 * the window-based algorithms - no boundary burst issue - at the cost of
 * O(limit) memory and pruning work per key.
 */
@Component
public class SlidingWindowLogRateLimiterStrategy implements RateLimiterStrategy {

    private final Clock clock;
    private final ConcurrentMap<String, Deque<Long>> logs = new ConcurrentHashMap<>();

    public SlidingWindowLogRateLimiterStrategy(Clock clock) {
        this.clock = clock;
    }

    @Override
    public RateLimiterAlgorithm getAlgorithm() {
        return RateLimiterAlgorithm.SLIDING_WINDOW_LOG;
    }

    @Override
    public RateLimitResult tryAcquire(String key, RateLimitConfig config) {
        SlidingWindowLogConfig cfg = as(config);
        long windowMillis = cfg.windowSize().toMillis();
        long now = clock.millis();
        long windowStart = now - windowMillis;

        Deque<Long> timestamps = logs.computeIfAbsent(key, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
                timestamps.pollFirst();
            }
            Instant resetAt = Instant.ofEpochMilli(
                    timestamps.isEmpty() ? now + windowMillis : timestamps.peekFirst() + windowMillis);
            if (timestamps.size() < cfg.limit()) {
                timestamps.addLast(now);
                return RateLimitResult.allow(cfg.limit() - timestamps.size(), resetAt);
            }
            return RateLimitResult.deny(resetAt.toEpochMilli() - now, resetAt);
        }
    }

    private SlidingWindowLogConfig as(RateLimitConfig config) {
        if (config instanceof SlidingWindowLogConfig slidingWindowLogConfig) {
            return slidingWindowLogConfig;
        }
        throw new IllegalArgumentException("Expected SlidingWindowLogConfig but got " + config.getClass().getSimpleName());
    }
}
