package com.example.ratelimiter.config;

/**
 * Configuration for the leaky-bucket algorithm: a bucket of size
 * {@code capacity} that drains at a constant {@code leakRatePerSecond}.
 * A request is admitted only if the bucket has room after accounting for
 * how much has leaked since the last request.
 */
public record LeakyBucketConfig(long capacity, double leakRatePerSecond) implements RateLimitConfig {

    public LeakyBucketConfig {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        if (leakRatePerSecond <= 0) {
            throw new IllegalArgumentException("leakRatePerSecond must be positive");
        }
    }

    @Override
    public RateLimiterAlgorithm algorithm() {
        return RateLimiterAlgorithm.LEAKY_BUCKET;
    }
}
