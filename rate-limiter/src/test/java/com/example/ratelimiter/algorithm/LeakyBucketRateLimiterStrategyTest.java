package com.example.ratelimiter.algorithm;

import com.example.ratelimiter.config.LeakyBucketConfig;
import com.example.ratelimiter.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class LeakyBucketRateLimiterStrategyTest {

    private MutableClock clock;
    private LeakyBucketRateLimiterStrategy strategy;
    private LeakyBucketConfig config;

    @BeforeEach
    void setUp() {
        clock = MutableClock.startingAt(Instant.parse("2026-01-01T00:00:00Z"));
        strategy = new LeakyBucketRateLimiterStrategy(clock);
        config = new LeakyBucketConfig(2, 1.0);
    }

    @Test
    void fillsBucketThenDeniesUntilItLeaks() {
        assertThat(strategy.tryAcquire("client", config).allowed()).isTrue();
        assertThat(strategy.tryAcquire("client", config).allowed()).isTrue();
        assertThat(strategy.tryAcquire("client", config).allowed()).isFalse();

        clock.advance(Duration.ofSeconds(1));

        assertThat(strategy.tryAcquire("client", config).allowed()).isTrue();
    }

    @Test
    void deniedResultReportsPositiveRetryAfter() {
        strategy.tryAcquire("client", config);
        strategy.tryAcquire("client", config);

        assertThat(strategy.tryAcquire("client", config).retryAfterMillis()).isGreaterThan(0);
    }
}
