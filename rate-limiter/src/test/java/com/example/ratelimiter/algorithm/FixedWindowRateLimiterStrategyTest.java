package com.example.ratelimiter.algorithm;

import com.example.ratelimiter.config.FixedWindowConfig;
import com.example.ratelimiter.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class FixedWindowRateLimiterStrategyTest {

    private MutableClock clock;
    private FixedWindowRateLimiterStrategy strategy;
    private FixedWindowConfig config;

    @BeforeEach
    void setUp() {
        clock = MutableClock.startingAt(Instant.parse("2026-01-01T00:00:00Z"));
        strategy = new FixedWindowRateLimiterStrategy(clock);
        config = new FixedWindowConfig(2, Duration.ofSeconds(1));
    }

    @Test
    void allowsUpToLimitWithinWindowThenDenies() {
        assertThat(strategy.tryAcquire("client", config).allowed()).isTrue();
        assertThat(strategy.tryAcquire("client", config).allowed()).isTrue();
        assertThat(strategy.tryAcquire("client", config).allowed()).isFalse();
    }

    @Test
    void resetsCountOnNextWindow() {
        strategy.tryAcquire("client", config);
        strategy.tryAcquire("client", config);
        assertThat(strategy.tryAcquire("client", config).allowed()).isFalse();

        clock.advance(Duration.ofSeconds(1));

        assertThat(strategy.tryAcquire("client", config).allowed()).isTrue();
    }
}
