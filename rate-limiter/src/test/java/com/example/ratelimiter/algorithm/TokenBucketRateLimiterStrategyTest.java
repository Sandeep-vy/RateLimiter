package com.example.ratelimiter.algorithm;

import com.example.ratelimiter.config.TokenBucketConfig;
import com.example.ratelimiter.core.RateLimitResult;
import com.example.ratelimiter.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBucketRateLimiterStrategyTest {

    private MutableClock clock;
    private TokenBucketRateLimiterStrategy strategy;
    private TokenBucketConfig config;

    @BeforeEach
    void setUp() {
        clock = MutableClock.startingAt(Instant.parse("2026-01-01T00:00:00Z"));
        strategy = new TokenBucketRateLimiterStrategy(clock);
        config = TokenBucketConfig.builder()
                .capacity(2)
                .refillTokens(2)
                .refillPeriod(Duration.ofSeconds(1))
                .build();
    }

    @Test
    void allowsRequestsUpToCapacityThenDenies() {
        assertThat(strategy.tryAcquire("client", config).allowed()).isTrue();
        assertThat(strategy.tryAcquire("client", config).allowed()).isTrue();

        RateLimitResult third = strategy.tryAcquire("client", config);
        assertThat(third.allowed()).isFalse();
        assertThat(third.retryAfterMillis()).isGreaterThan(0);
    }

    @Test
    void refillsTokensAfterElapsedPeriod() {
        strategy.tryAcquire("client", config);
        strategy.tryAcquire("client", config);
        assertThat(strategy.tryAcquire("client", config).allowed()).isFalse();

        clock.advance(Duration.ofSeconds(1));

        assertThat(strategy.tryAcquire("client", config).allowed()).isTrue();
    }

    @Test
    void tracksSeparateKeysIndependently() {
        strategy.tryAcquire("client-a", config);
        strategy.tryAcquire("client-a", config);

        assertThat(strategy.tryAcquire("client-a", config).allowed()).isFalse();
        assertThat(strategy.tryAcquire("client-b", config).allowed()).isTrue();
    }
}
