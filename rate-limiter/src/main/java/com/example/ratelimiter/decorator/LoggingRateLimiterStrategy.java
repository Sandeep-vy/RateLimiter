package com.example.ratelimiter.decorator;

import com.example.ratelimiter.algorithm.RateLimiterStrategy;
import com.example.ratelimiter.config.RateLimitConfig;
import com.example.ratelimiter.config.RateLimiterAlgorithm;
import com.example.ratelimiter.core.RateLimitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator pattern: wraps any {@link RateLimiterStrategy} to log every
 * decision without the underlying algorithm implementations needing to know
 * about logging at all. Kept separate from the strategies themselves so a
 * cross-cutting concern (observability) doesn't leak into algorithm logic -
 * new decorators (metrics, tracing, auditing) can be layered the same way.
 */
public class LoggingRateLimiterStrategy implements RateLimiterStrategy {

    private static final Logger log = LoggerFactory.getLogger(LoggingRateLimiterStrategy.class);

    private final RateLimiterStrategy delegate;

    public LoggingRateLimiterStrategy(RateLimiterStrategy delegate) {
        this.delegate = delegate;
    }

    @Override
    public RateLimiterAlgorithm getAlgorithm() {
        return delegate.getAlgorithm();
    }

    @Override
    public RateLimitResult tryAcquire(String key, RateLimitConfig config) {
        RateLimitResult result = delegate.tryAcquire(key, config);
        if (result.allowed()) {
            log.debug("[{}] ALLOWED key={} remaining={}", delegate.getAlgorithm(), key, result.remainingPermits());
        } else {
            log.warn("[{}] THROTTLED key={} retryAfterMs={}", delegate.getAlgorithm(), key, result.retryAfterMillis());
        }
        return result;
    }
}
