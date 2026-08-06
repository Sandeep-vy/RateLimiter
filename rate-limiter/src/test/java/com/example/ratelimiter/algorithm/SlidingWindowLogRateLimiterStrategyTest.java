package com.example.ratelimiter.algorithm;

import com.example.ratelimiter.config.SlidingWindowLogConfig;
import com.example.ratelimiter.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SlidingWindowLogRateLimiterStrategyTest {

    private MutableClock clock;
    private SlidingWindowLogRateLimiterStrategy strategy;
    private SlidingWindowLogConfig config;

    @BeforeEach
    void setUp() {
        clock = MutableClock.startingAt(Instant.parse("2026-01-01T00:00:00Z"));
        strategy = new SlidingWindowLogRateLimiterStrategy(clock);
        config = new SlidingWindowLogConfig(2, Duration.ofSeconds(1));
    }

    @Test
    void allowsUpToLimitThenDeniesWithinWindow() {
        assertThat(strategy.tryAcquire("client", config).allowed()).isTrue();
        assertThat(strategy.tryAcquire("client", config).allowed()).isTrue();
        assertThat(strategy.tryAcquire("client", config).allowed()).isFalse();
    }

    @Test
    void slidesRatherThanResettingOnBoundary() {
        strategy.tryAcquire("client", config);
        clock.advance(Duration.ofMillis(500));
        strategy.tryAcquire("client", config);

        assertThat(strategy.tryAcquire("client", config).allowed()).isFalse();

        clock.advance(Duration.ofMillis(501));
        assertThat(strategy.tryAcquire("client", config).allowed()).isTrue();
        assertThat(strategy.tryAcquire("client", config).allowed()).isFalse();
    }
}
