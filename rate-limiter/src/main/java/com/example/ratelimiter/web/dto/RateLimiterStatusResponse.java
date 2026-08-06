package com.example.ratelimiter.web.dto;

import com.example.ratelimiter.config.RateLimitConfig;
import com.example.ratelimiter.config.RateLimiterAlgorithm;
import com.example.ratelimiter.core.RateLimiter;

public record RateLimiterStatusResponse(
        String resourceId,
        RateLimiterAlgorithm algorithm,
        RateLimitConfig config,
        long throttledSoFar
) {
    public static RateLimiterStatusResponse from(RateLimiter limiter, long throttledSoFar) {
        return new RateLimiterStatusResponse(
                limiter.getResourceId(), limiter.currentAlgorithm(), limiter.currentConfig(), throttledSoFar);
    }
}
