package com.example.ratelimiter.algorithm;

import com.example.ratelimiter.config.SlidingWindowCounterConfig;
import com.example.ratelimiter.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SlidingWindowCounterRateLimiterStrategyTest {

    private MutableClock clock;
    private SlidingWindowCounterRateLimiterStrategy strategy;
    private SlidingWindowCounterConfig config;

    @BeforeEach
    void setUp() {
        clock = MutableClock.startingAt(Instant.parse("2026-01-01T00:00:00Z"));
        strategy = new SlidingWindowCounterRateLimiterStrategy(clock);
        config = new SlidingWindowCounterConfig(4, Duration.ofSeconds(1));
    }

    @Test
    void allowsUpToLimitThenDeniesWithinWindow() {
        for (int i = 0; i < 4; i++) {
            assertThat(strategy.tryAcquire("client", config).allowed()).isTrue();
        }
        assertThat(strategy.tryAcquire("client", config).allowed()).isFalse();
    }

    @Test
    void deniesImmediatelyAtWindowRolloverWhenPreviousWindowWasFull() {
        for (int i = 0; i < 4; i++) {
            strategy.tryAcquire("client", config);
        }
        clock.advance(Duration.ofMillis(1000));

        // Right at rollover, overlap with the just-elapsed full previous window is 100%,
        // so the estimate is still at the limit - this is what smooths out the burst a
        // fixed-window counter would allow at the exact same instant.
        assertThat(strategy.tryAcquire("client", config).allowed()).isFalse();
    }

    @Test
    void allowsFullQuotaAgainOncePreviousWindowHasFullyElapsed() {
        for (int i = 0; i < 4; i++) {
            strategy.tryAcquire("client", config);
        }
        clock.advance(Duration.ofMillis(2000));

        for (int i = 0; i < 4; i++) {
            assertThat(strategy.tryAcquire("client", config).allowed()).isTrue();
        }
        assertThat(strategy.tryAcquire("client", config).allowed()).isFalse();
    }
}
