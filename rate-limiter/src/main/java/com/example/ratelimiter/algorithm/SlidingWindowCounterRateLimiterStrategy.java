package com.example.ratelimiter.algorithm;

import com.example.ratelimiter.config.RateLimitConfig;
import com.example.ratelimiter.config.RateLimiterAlgorithm;
import com.example.ratelimiter.config.SlidingWindowCounterConfig;
import com.example.ratelimiter.core.RateLimitResult;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Sliding-window-counter: an O(1)-memory approximation of the sliding-window
 * log. Keeps only a current-window and previous-window counter, and weights
 * the previous window's count by how much of it still overlaps the trailing
 * {@code windowSize} interval: {@code estimate = currentCount +
 * previousCount * (overlapFraction)}. Slightly less precise than the log
 * variant at window boundaries, but memory cost per key is constant instead
 * of proportional to traffic.
 */
@Component
public class SlidingWindowCounterRateLimiterStrategy implements RateLimiterStrategy {

    private final Clock clock;
    private final ConcurrentMap<String, CounterState> counters = new ConcurrentHashMap<>();

    public SlidingWindowCounterRateLimiterStrategy(Clock clock) {
        this.clock = clock;
    }

    @Override
    public RateLimiterAlgorithm getAlgorithm() {
        return RateLimiterAlgorithm.SLIDING_WINDOW_COUNTER;
    }

    @Override
    public RateLimitResult tryAcquire(String key, RateLimitConfig config) {
        SlidingWindowCounterConfig cfg = as(config);
        long windowMillis = cfg.windowSize().toMillis();
        long now = clock.millis();
        long currentWindowStart = (now / windowMillis) * windowMillis;

        CounterState state = counters.computeIfAbsent(key, k -> new CounterState(currentWindowStart, 0, 0));

        synchronized (state) {
            advanceWindow(state, currentWindowStart, windowMillis);

            double elapsedInCurrent = now - state.currentWindowStart;
            double overlapFraction = Math.max(0.0, (windowMillis - elapsedInCurrent) / windowMillis);
            double estimate = state.currentCount + state.previousCount * overlapFraction;

            Instant resetAt = Instant.ofEpochMilli(state.currentWindowStart + windowMillis);
            if (estimate < cfg.limit()) {
                state.currentCount++;
                long remaining = Math.max(0, (long) (cfg.limit() - estimate - 1));
                return RateLimitResult.allow(remaining, resetAt);
            }
            return RateLimitResult.deny(resetAt.toEpochMilli() - now, resetAt);
        }
    }

    private void advanceWindow(CounterState state, long currentWindowStart, long windowMillis) {
        if (state.currentWindowStart == currentWindowStart) {
            return;
        }
        long windowsElapsed = (currentWindowStart - state.currentWindowStart) / windowMillis;
        if (windowsElapsed == 1) {
            state.previousCount = state.currentCount;
        } else {
            state.previousCount = 0;
        }
        state.currentCount = 0;
        state.currentWindowStart = currentWindowStart;
    }

    private SlidingWindowCounterConfig as(RateLimitConfig config) {
        if (config instanceof SlidingWindowCounterConfig slidingWindowCounterConfig) {
            return slidingWindowCounterConfig;
        }
        throw new IllegalArgumentException("Expected SlidingWindowCounterConfig but got " + config.getClass().getSimpleName());
    }

    private static final class CounterState {
        private long currentWindowStart;
        private long currentCount;
        private long previousCount;

        private CounterState(long currentWindowStart, long currentCount, long previousCount) {
            this.currentWindowStart = currentWindowStart;
            this.currentCount = currentCount;
            this.previousCount = previousCount;
        }
    }
}
