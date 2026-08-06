package com.example.ratelimiter.config;

/**
 * Marker abstraction for algorithm-specific configuration. Sealing the
 * hierarchy lets every {@code switch} over the permitted types be exhaustive
 * at compile time - adding a sixth algorithm forces every mapper/switch to be
 * revisited instead of silently falling through a {@code default} branch.
 */
public sealed interface RateLimitConfig
        permits TokenBucketConfig, LeakyBucketConfig, FixedWindowConfig,
        SlidingWindowLogConfig, SlidingWindowCounterConfig {

    RateLimiterAlgorithm algorithm();
}
