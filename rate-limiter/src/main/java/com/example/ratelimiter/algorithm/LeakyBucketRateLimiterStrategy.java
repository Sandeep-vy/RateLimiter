package com.example.ratelimiter.algorithm;

import com.example.ratelimiter.config.LeakyBucketConfig;
import com.example.ratelimiter.config.RateLimitConfig;
import com.example.ratelimiter.config.RateLimiterAlgorithm;
import com.example.ratelimiter.core.RateLimitResult;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Leaky-bucket: each key owns a "water level" that drains at a constant
 * {@code leakRatePerSecond} and rises by one unit per admitted request. A
 * request is admitted only if, after draining for elapsed time, the level
 * still has room under {@code capacity}. Unlike token-bucket it enforces a
 * strictly constant outflow rather than allowing saved-up bursts.
 */
@Component
public class LeakyBucketRateLimiterStrategy implements RateLimiterStrategy {

    private final Clock clock;
    private final ConcurrentMap<String, BucketState> buckets = new ConcurrentHashMap<>();

    public LeakyBucketRateLimiterStrategy(Clock clock) {
        this.clock = clock;
    }

    @Override
    public RateLimiterAlgorithm getAlgorithm() {
        return RateLimiterAlgorithm.LEAKY_BUCKET;
    }

    @Override
    public RateLimitResult tryAcquire(String key, RateLimitConfig config) {
        LeakyBucketConfig cfg = as(config);
        BucketState state = buckets.computeIfAbsent(key, k -> new BucketState(0.0, clock.millis()));

        synchronized (state) {
            leak(state, cfg);
            Instant resetAt = Instant.ofEpochMilli(state.lastLeakMillis + Math.round(1000.0 / cfg.leakRatePerSecond()));
            if (state.level < cfg.capacity()) {
                state.level += 1.0;
                long remaining = Math.max(cfg.capacity() - Math.round(state.level), 0);
                return RateLimitResult.allow(remaining, resetAt);
            }
            long overflow = Math.round(state.level - cfg.capacity() + 1);
            long retryAfterMillis = Math.round((overflow / cfg.leakRatePerSecond()) * 1000.0);
            return RateLimitResult.deny(retryAfterMillis, resetAt);
        }
    }

    private void leak(BucketState state, LeakyBucketConfig cfg) {
        long now = clock.millis();
        double elapsedSeconds = (now - state.lastLeakMillis) / 1000.0;
        if (elapsedSeconds <= 0) {
            return;
        }
        double leaked = elapsedSeconds * cfg.leakRatePerSecond();
        state.level = Math.max(0.0, state.level - leaked);
        state.lastLeakMillis = now;
    }

    private LeakyBucketConfig as(RateLimitConfig config) {
        if (config instanceof LeakyBucketConfig leakyBucketConfig) {
            return leakyBucketConfig;
        }
        throw new IllegalArgumentException("Expected LeakyBucketConfig but got " + config.getClass().getSimpleName());
    }

    private static final class BucketState {
        private double level;
        private long lastLeakMillis;

        private BucketState(double level, long lastLeakMillis) {
            this.level = level;
            this.lastLeakMillis = lastLeakMillis;
        }
    }
}
