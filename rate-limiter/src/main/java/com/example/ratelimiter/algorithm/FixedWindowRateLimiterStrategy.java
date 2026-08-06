package com.example.ratelimiter.algorithm;

import com.example.ratelimiter.config.FixedWindowConfig;
import com.example.ratelimiter.config.RateLimitConfig;
import com.example.ratelimiter.config.RateLimiterAlgorithm;
import com.example.ratelimiter.core.RateLimitResult;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Fixed-window: time is sliced into non-overlapping {@code windowSize}
 * buckets aligned to the epoch; at most {@code limit} requests are admitted
 * per window. Cheapest algorithm to implement and reason about, but allows
 * up to {@code 2 * limit} requests across a window boundary (e.g. a burst at
 * the very end of one window followed by another at the very start of the
 * next).
 */
@Component
public class FixedWindowRateLimiterStrategy implements RateLimiterStrategy {

    private final Clock clock;
    private final ConcurrentMap<String, WindowState> windows = new ConcurrentHashMap<>();

    public FixedWindowRateLimiterStrategy(Clock clock) {
        this.clock = clock;
    }

    @Override
    public RateLimiterAlgorithm getAlgorithm() {
        return RateLimiterAlgorithm.FIXED_WINDOW;
    }

    @Override
    public RateLimitResult tryAcquire(String key, RateLimitConfig config) {
        FixedWindowConfig cfg = as(config);
        long windowMillis = cfg.windowSize().toMillis();
        long now = clock.millis();
        long currentWindowStart = (now / windowMillis) * windowMillis;

        WindowState state = windows.computeIfAbsent(key, k -> new WindowState(currentWindowStart, 0));

        synchronized (state) {
            if (state.windowStart != currentWindowStart) {
                state.windowStart = currentWindowStart;
                state.count = 0;
            }
            Instant resetAt = Instant.ofEpochMilli(state.windowStart + windowMillis);
            if (state.count < cfg.limit()) {
                state.count++;
                return RateLimitResult.allow(cfg.limit() - state.count, resetAt);
            }
            return RateLimitResult.deny(resetAt.toEpochMilli() - now, resetAt);
        }
    }

    private FixedWindowConfig as(RateLimitConfig config) {
        if (config instanceof FixedWindowConfig fixedWindowConfig) {
            return fixedWindowConfig;
        }
        throw new IllegalArgumentException("Expected FixedWindowConfig but got " + config.getClass().getSimpleName());
    }

    private static final class WindowState {
        private long windowStart;
        private long count;

        private WindowState(long windowStart, long count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
