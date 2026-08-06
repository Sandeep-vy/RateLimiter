package com.example.ratelimiter.core;

import java.time.Instant;

/**
 * Outcome of a single {@code tryAcquire} call. Immutable value object shared
 * by every algorithm so callers (service layer, web interceptor) depend on
 * one uniform result shape regardless of which strategy produced it.
 */
public record RateLimitResult(boolean allowed, long remainingPermits, long retryAfterMillis, Instant resetAt) {

    public static RateLimitResult allow(long remainingPermits, Instant resetAt) {
        return new RateLimitResult(true, remainingPermits, 0L, resetAt);
    }

    public static RateLimitResult deny(long retryAfterMillis, Instant resetAt) {
        return new RateLimitResult(false, 0L, retryAfterMillis, resetAt);
    }
}
