package com.example.ratelimiter.config;

import java.time.Duration;

/**
 * Configuration for the token-bucket algorithm: a bucket holding at most
 * {@code capacity} tokens, replenished by {@code refillTokens} every
 * {@code refillPeriod}. Exposes a {@link Builder} rather than the raw record
 * constructor so callers get named, order-independent parameters and a
 * single validation point.
 */
public record TokenBucketConfig(long capacity, long refillTokens, Duration refillPeriod) implements RateLimitConfig {

    public TokenBucketConfig {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        if (refillTokens <= 0) {
            throw new IllegalArgumentException("refillTokens must be positive");
        }
        if (refillPeriod == null || refillPeriod.isZero() || refillPeriod.isNegative()) {
            throw new IllegalArgumentException("refillPeriod must be positive");
        }
    }

    @Override
    public RateLimiterAlgorithm algorithm() {
        return RateLimiterAlgorithm.TOKEN_BUCKET;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Builder pattern: assembles a {@link TokenBucketConfig} field-by-field with fluent, named setters. */
    public static final class Builder {
        private long capacity;
        private long refillTokens;
        private Duration refillPeriod;

        private Builder() {
        }

        public Builder capacity(long capacity) {
            this.capacity = capacity;
            return this;
        }

        public Builder refillTokens(long refillTokens) {
            this.refillTokens = refillTokens;
            return this;
        }

        public Builder refillPeriod(Duration refillPeriod) {
            this.refillPeriod = refillPeriod;
            return this;
        }

        public TokenBucketConfig build() {
            return new TokenBucketConfig(capacity, refillTokens, refillPeriod);
        }
    }
}
