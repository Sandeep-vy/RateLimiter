package com.example.ratelimiter.config;

import java.time.Duration;

/**
 * Configuration for the fixed-window algorithm: at most {@code limit}
 * requests are admitted per non-overlapping {@code windowSize} interval.
 * Simple and cheap, but bursty at window boundaries.
 */
public record FixedWindowConfig(long limit, Duration windowSize) implements RateLimitConfig {

    public FixedWindowConfig {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }
        if (windowSize == null || windowSize.isZero() || windowSize.isNegative()) {
            throw new IllegalArgumentException("windowSize must be positive");
        }
    }

    @Override
    public RateLimiterAlgorithm algorithm() {
        return RateLimiterAlgorithm.FIXED_WINDOW;
    }
}
