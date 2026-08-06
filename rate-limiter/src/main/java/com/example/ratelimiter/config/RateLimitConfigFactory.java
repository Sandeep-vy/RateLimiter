package com.example.ratelimiter.config;

import com.example.ratelimiter.web.dto.RateLimitConfigRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Factory Method: turns the flat, wire-level {@link RateLimitConfigRequest}
 * into the precise, sealed {@link RateLimitConfig} the rest of the system
 * works with. Centralizing this here means the web layer never constructs
 * domain config objects directly, and adding a new algorithm only requires
 * touching this one switch (the compiler flags it since the source enum is
 * exhaustively switched over).
 */
@Component
public class RateLimitConfigFactory {

    public RateLimitConfig from(RateLimitConfigRequest request) {
        return switch (request.algorithm()) {
            case TOKEN_BUCKET -> TokenBucketConfig.builder()
                    .capacity(require(request.capacity(), "capacity"))
                    .refillTokens(require(request.refillTokens(), "refillTokens"))
                    .refillPeriod(Duration.ofMillis(require(request.refillPeriodMillis(), "refillPeriodMillis")))
                    .build();
            case LEAKY_BUCKET -> new LeakyBucketConfig(
                    require(request.capacity(), "capacity"),
                    require(request.leakRatePerSecond(), "leakRatePerSecond"));
            case FIXED_WINDOW -> new FixedWindowConfig(
                    require(request.limit(), "limit"),
                    Duration.ofMillis(require(request.windowMillis(), "windowMillis")));
            case SLIDING_WINDOW_LOG -> new SlidingWindowLogConfig(
                    require(request.limit(), "limit"),
                    Duration.ofMillis(require(request.windowMillis(), "windowMillis")));
            case SLIDING_WINDOW_COUNTER -> new SlidingWindowCounterConfig(
                    require(request.limit(), "limit"),
                    Duration.ofMillis(require(request.windowMillis(), "windowMillis")));
        };
    }

    private static <T> T require(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException("Missing required field for this algorithm: " + field);
        }
        return value;
    }
}
