package com.example.ratelimiter.core;

import com.example.ratelimiter.algorithm.FixedWindowRateLimiterStrategy;
import com.example.ratelimiter.algorithm.TokenBucketRateLimiterStrategy;
import com.example.ratelimiter.config.FixedWindowConfig;
import com.example.ratelimiter.config.TokenBucketConfig;
import com.example.ratelimiter.factory.RateLimiterStrategyFactory;
import com.example.ratelimiter.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Centerpiece test: proves a resource's algorithm can be switched at runtime,
 * mid-traffic, without losing the resource identity or requiring a restart -
 * exactly the requirement this whole design exists to satisfy.
 */
class RateLimiterRegistryRuntimeSwitchTest {

    private MutableClock clock;
    private RateLimiterRegistry registry;

    @BeforeEach
    void setUp() {
        clock = MutableClock.startingAt(Instant.parse("2026-01-01T00:00:00Z"));
        RateLimiterStrategyFactory factory = new RateLimiterStrategyFactory(List.of(
                new TokenBucketRateLimiterStrategy(clock),
                new FixedWindowRateLimiterStrategy(clock)));
        registry = new RateLimiterRegistry(factory);
    }

    @Test
    void switchingAlgorithmPreservesResourceIdentityAndAppliesNewRulesImmediately() {
        TokenBucketConfig tokenBucketConfig = TokenBucketConfig.builder()
                .capacity(1).refillTokens(1).refillPeriod(Duration.ofSeconds(10)).build();
        registry.register("orders-api", tokenBucketConfig);

        RateLimiter limiter = registry.get("orders-api");
        assertThat(limiter.tryAcquire("client").allowed()).isTrue();
        assertThat(limiter.tryAcquire("client").allowed()).isFalse();

        FixedWindowConfig fixedWindowConfig = new FixedWindowConfig(3, Duration.ofSeconds(1));
        registry.register("orders-api", fixedWindowConfig);

        RateLimiter sameLimiter = registry.get("orders-api");
        assertThat(sameLimiter).isSameAs(limiter);
        assertThat(sameLimiter.currentAlgorithm()).isEqualTo(fixedWindowConfig.algorithm());

        assertThat(sameLimiter.tryAcquire("client").allowed()).isTrue();
        assertThat(sameLimiter.tryAcquire("client").allowed()).isTrue();
        assertThat(sameLimiter.tryAcquire("client").allowed()).isTrue();
        assertThat(sameLimiter.tryAcquire("client").allowed()).isFalse();
    }

    @Test
    void unknownResourceLookupFails() {
        assertThat(registry.isRegistered("missing")).isFalse();
    }
}
