package com.example.ratelimiter.exception;

import com.example.ratelimiter.core.RateLimitResult;

/**
 * Raised by the web interceptor when a request is throttled, so
 * {@code GlobalExceptionHandler} can translate it into a 429 response
 * carrying the retry-after hint from the originating {@link RateLimitResult}.
 */
public class RateLimitExceededException extends RuntimeException {

    private final RateLimitResult result;

    public RateLimitExceededException(String resourceId, RateLimitResult result) {
        super("Rate limit exceeded for resource: " + resourceId);
        this.result = result;
    }

    public RateLimitResult getResult() {
        return result;
    }
}
