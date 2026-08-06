package com.example.ratelimiter.config;

import java.time.Duration;

/**
 * Configuration for the sliding-window-counter algorithm: an approximation
 * of the sliding-window-log that keeps only two counters (current and
 * previous window) and weights the previous one by its overlap with the
 * trailing window, trading a small accuracy loss for O(1) memory per key.
 */
public record SlidingWindowCounterConfig(long limit, Duration windowSize) implements RateLimitConfig {

    public SlidingWindowCounterConfig {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }
        if (windowSize == null || windowSize.isZero() || windowSize.isNegative()) {
            throw new IllegalArgumentException("windowSize must be positive");
        }
    }

    @Override
    public RateLimiterAlgorithm algorithm() {
        return RateLimiterAlgorithm.SLIDING_WINDOW_COUNTER;
    }
}
