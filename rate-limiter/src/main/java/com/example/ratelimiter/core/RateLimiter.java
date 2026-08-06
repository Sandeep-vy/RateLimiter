package com.example.ratelimiter.core;

import com.example.ratelimiter.algorithm.RateLimiterStrategy;
import com.example.ratelimiter.config.RateLimitConfig;
import com.example.ratelimiter.config.RateLimiterAlgorithm;

/**
 * Strategy-pattern context object bound to one resource (e.g. an API route
 * or a tenant tier). Holds a swappable {@link RateLimiterStrategy} plus the
 * {@link RateLimitConfig} it should be evaluated with; {@link #reconfigure}
 * is the single seam that lets {@code RateLimiterRegistry} change the
 * algorithm for this resource at runtime without callers ever holding a
 * stale reference - they always go through this object.
 *
 * <p>Reads happen far more often than reconfiguration, so the strategy/config
 * pair is held in {@code volatile} fields rather than behind a lock: readers
 * always see either the old pair or the new one, never a half-updated mix,
 * because both fields are only ever replaced together under
 * {@link #reconfigure}'s synchronized block.
 */
public final class RateLimiter {

    private final String resourceId;
    private volatile RateLimiterStrategy strategy;
    private volatile RateLimitConfig config;

    RateLimiter(String resourceId, RateLimiterStrategy strategy, RateLimitConfig config) {
        this.resourceId = resourceId;
        this.strategy = strategy;
        this.config = config;
    }

    public RateLimitResult tryAcquire(String clientKey) {
        return strategy.tryAcquire(namespacedKey(clientKey), config);
    }

    synchronized void reconfigure(RateLimiterStrategy newStrategy, RateLimitConfig newConfig) {
        this.strategy = newStrategy;
        this.config = newConfig;
    }

    public String getResourceId() {
        return resourceId;
    }

    public RateLimiterAlgorithm currentAlgorithm() {
        return strategy.getAlgorithm();
    }

    public RateLimitConfig currentConfig() {
        return config;
    }

    /** Prevents state collisions when a shared, singleton strategy bean serves multiple resources. */
    private String namespacedKey(String clientKey) {
        return resourceId + ':' + clientKey;
    }
}
