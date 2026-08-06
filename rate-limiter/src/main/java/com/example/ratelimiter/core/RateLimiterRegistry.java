package com.example.ratelimiter.core;

import com.example.ratelimiter.algorithm.RateLimiterStrategy;
import com.example.ratelimiter.config.RateLimitConfig;
import com.example.ratelimiter.decorator.LoggingRateLimiterStrategy;
import com.example.ratelimiter.exception.UnknownResourceException;
import com.example.ratelimiter.factory.RateLimiterStrategyFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Singleton (a single Spring-managed instance for the whole application)
 * registry of {@link RateLimiter} contexts, one per resource id. This is the
 * component that makes runtime algorithm-swapping possible: {@link #register}
 * either creates a fresh {@code RateLimiter} or, if the resource already
 * exists, reconfigures it in place with a freshly-resolved strategy - every
 * caller holding a reference to that resource's {@code RateLimiter} picks up
 * the new algorithm on its very next call, with no restart and no downtime.
 */
@Component
public class RateLimiterRegistry {

    private final RateLimiterStrategyFactory strategyFactory;
    private final ConcurrentMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    public RateLimiterRegistry(RateLimiterStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    public RateLimiter register(String resourceId, RateLimitConfig config) {
        return limiters.compute(resourceId, (id, existing) -> {
            RateLimiterStrategy strategy = decorate(strategyFactory.create(config.algorithm()));
            if (existing == null) {
                return new RateLimiter(id, strategy, config);
            }
            existing.reconfigure(strategy, config);
            return existing;
        });
    }

    public RateLimiter get(String resourceId) {
        RateLimiter limiter = limiters.get(resourceId);
        if (limiter == null) {
            throw new UnknownResourceException(resourceId);
        }
        return limiter;
    }

    public boolean isRegistered(String resourceId) {
        return limiters.containsKey(resourceId);
    }

    public Map<String, RateLimiter> snapshot() {
        return Map.copyOf(limiters);
    }

    private RateLimiterStrategy decorate(RateLimiterStrategy strategy) {
        return new LoggingRateLimiterStrategy(strategy);
    }
}
