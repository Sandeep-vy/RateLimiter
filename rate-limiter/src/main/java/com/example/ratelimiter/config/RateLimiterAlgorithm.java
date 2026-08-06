package com.example.ratelimiter.config;

/**
 * Identifies every algorithm the system knows how to execute. The
 * {@code RateLimiterStrategyFactory} uses this as the lookup key into the
 * pool of {@code RateLimiterStrategy} beans, and every {@link RateLimitConfig}
 * declares which algorithm it configures.
 */
public enum RateLimiterAlgorithm {
    TOKEN_BUCKET,
    LEAKY_BUCKET,
    FIXED_WINDOW,
    SLIDING_WINDOW_LOG,
    SLIDING_WINDOW_COUNTER
}
