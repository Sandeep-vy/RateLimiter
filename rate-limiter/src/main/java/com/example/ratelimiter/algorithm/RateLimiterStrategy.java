package com.example.ratelimiter.algorithm;

import com.example.ratelimiter.config.RateLimitConfig;
import com.example.ratelimiter.config.RateLimiterAlgorithm;
import com.example.ratelimiter.core.RateLimitResult;

/**
 * Strategy pattern: one implementation per rate-limiting algorithm. All
 * implementations are stateful (they track per-key usage internally) but
 * stateless with respect to configuration - the caller passes the
 * {@link RateLimitConfig} on every call, which is what makes a strategy
 * instance safely reusable across many independently-configured resources
 * and swappable at runtime without losing thread-safety.
 */
public interface RateLimiterStrategy {

    RateLimiterAlgorithm getAlgorithm();

    /**
     * Attempts to consume one permit for {@code key}.
     *
     * @param key    a caller-scoped identifier, already namespaced by resource
     *               so that unrelated resources sharing this strategy instance
     *               never collide on internal state
     * @param config the algorithm-specific configuration to evaluate against
     */
    RateLimitResult tryAcquire(String key, RateLimitConfig config);
}
