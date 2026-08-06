package com.example.ratelimiter.algorithm;

import com.example.ratelimiter.config.RateLimitConfig;
import com.example.ratelimiter.config.RateLimiterAlgorithm;
import com.example.ratelimiter.config.TokenBucketConfig;
import com.example.ratelimiter.core.RateLimitResult;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Token-bucket: each key owns a bucket that starts full and refills
 * {@code refillTokens} every {@code refillPeriod}, capped at {@code capacity}.
 * A request is admitted if the bucket has at least one token after refilling
 * for elapsed time. Smooths bursts better than fixed windows since capacity
 * saved up during idle periods can absorb a short spike.
 */
@Component
public class TokenBucketRateLimiterStrategy implements RateLimiterStrategy {

    private final Clock clock;
    private final ConcurrentMap<String, BucketState> buckets = new ConcurrentHashMap<>();

    public TokenBucketRateLimiterStrategy(Clock clock) {
        this.clock = clock;
    }

    @Override
    public RateLimiterAlgorithm getAlgorithm() {
        return RateLimiterAlgorithm.TOKEN_BUCKET;
    }

    @Override
    public RateLimitResult tryAcquire(String key, RateLimitConfig config) {
        TokenBucketConfig cfg = as(config);
        BucketState state = buckets.computeIfAbsent(key, k -> new BucketState(cfg.capacity(), clock.millis()));

        synchronized (state) {
            refill(state, cfg);
            Instant resetAt = Instant.ofEpochMilli(state.lastRefillMillis + cfg.refillPeriod().toMillis());
            if (state.tokens > 0) {
                state.tokens--;
                return RateLimitResult.allow(state.tokens, resetAt);
            }
            long retryAfter = cfg.refillPeriod().toMillis() - (clock.millis() - state.lastRefillMillis);
            return RateLimitResult.deny(Math.max(retryAfter, 0), resetAt);
        }
    }

    private void refill(BucketState state, TokenBucketConfig cfg) {
        long now = clock.millis();
        long elapsed = now - state.lastRefillMillis;
        long refillPeriodMillis = cfg.refillPeriod().toMillis();
        if (elapsed < refillPeriodMillis) {
            return;
        }
        long periods = elapsed / refillPeriodMillis;
        state.tokens = Math.min(cfg.capacity(), state.tokens + periods * cfg.refillTokens());
        state.lastRefillMillis += periods * refillPeriodMillis;
    }

    private TokenBucketConfig as(RateLimitConfig config) {
        if (config instanceof TokenBucketConfig tokenBucketConfig) {
            return tokenBucketConfig;
        }
        throw new IllegalArgumentException("Expected TokenBucketConfig but got " + config.getClass().getSimpleName());
    }

    private static final class BucketState {
        private long tokens;
        private long lastRefillMillis;

        private BucketState(long tokens, long lastRefillMillis) {
            this.tokens = tokens;
            this.lastRefillMillis = lastRefillMillis;
        }
    }
}
