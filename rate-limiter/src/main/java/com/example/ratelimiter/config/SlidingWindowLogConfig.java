package com.example.ratelimiter.config;

import java.time.Duration;

/**
 * Configuration for the sliding-window-log algorithm: at most {@code limit}
 * requests are admitted in any trailing {@code windowSize} interval, tracked
 * with per-request timestamps. Precise but memory cost scales with traffic.
 */
public record SlidingWindowLogConfig(long limit, Duration windowSize) implements RateLimitConfig {

    public SlidingWindowLogConfig {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }
        if (windowSize == null || windowSize.isZero() || windowSize.isNegative()) {
            throw new IllegalArgumentException("windowSize must be positive");
        }
    }

    @Override
    public RateLimiterAlgorithm algorithm() {
        return RateLimiterAlgorithm.SLIDING_WINDOW_LOG;
    }
}
