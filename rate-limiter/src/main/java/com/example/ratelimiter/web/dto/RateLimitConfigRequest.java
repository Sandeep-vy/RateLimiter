package com.example.ratelimiter.web.dto;

import com.example.ratelimiter.config.RateLimiterAlgorithm;
import jakarta.validation.constraints.NotNull;

/**
 * Transport-level shape for configuring or reconfiguring a resource. It is
 * intentionally flat and over-inclusive (every algorithm's fields, all
 * optional except {@code algorithm}) because a JSON request body cannot be
 * sealed the way {@link com.example.ratelimiter.config.RateLimitConfig} is;
 * {@code RateLimitConfigFactory} is where the flat shape gets narrowed back
 * into a precise, validated domain type.
 */
public record RateLimitConfigRequest(
        @NotNull RateLimiterAlgorithm algorithm,
        Long capacity,
        Long refillTokens,
        Long refillPeriodMillis,
        Long limit,
        Long windowMillis,
        Double leakRatePerSecond
) {
}
