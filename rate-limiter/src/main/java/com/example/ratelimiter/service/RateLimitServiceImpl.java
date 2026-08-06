package com.example.ratelimiter.service;

import com.example.ratelimiter.config.RateLimitConfig;
import com.example.ratelimiter.core.RateLimitResult;
import com.example.ratelimiter.core.RateLimiter;
import com.example.ratelimiter.core.RateLimiterRegistry;
import com.example.ratelimiter.event.RateLimitExceededEvent;
import com.example.ratelimiter.event.RateLimitMetricsListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Default facade implementation. Depends only on the {@link RateLimiterRegistry}
 * abstraction and Spring's {@link ApplicationEventPublisher} - never on a
 * concrete algorithm - so it needs no changes when algorithms are added,
 * removed, or swapped at runtime (Dependency Inversion in practice).
 */
@Service
public class RateLimitServiceImpl implements RateLimitService {

    private final RateLimiterRegistry registry;
    private final ApplicationEventPublisher eventPublisher;
    private final RateLimitMetricsListener metricsListener;

    public RateLimitServiceImpl(RateLimiterRegistry registry,
                                 ApplicationEventPublisher eventPublisher,
                                 RateLimitMetricsListener metricsListener) {
        this.registry = registry;
        this.eventPublisher = eventPublisher;
        this.metricsListener = metricsListener;
    }

    @Override
    public RateLimitResult tryAcquire(String resourceId, String clientKey) {
        RateLimiter limiter = registry.get(resourceId);
        RateLimitResult result = limiter.tryAcquire(clientKey);
        if (!result.allowed()) {
            eventPublisher.publishEvent(
                    new RateLimitExceededEvent(resourceId, clientKey, limiter.currentAlgorithm(), result));
        }
        return result;
    }

    @Override
    public RateLimiter registerOrReconfigure(String resourceId, RateLimitConfig config) {
        return registry.register(resourceId, config);
    }

    @Override
    public RateLimiter getResource(String resourceId) {
        return registry.get(resourceId);
    }

    @Override
    public boolean isRegistered(String resourceId) {
        return registry.isRegistered(resourceId);
    }

    @Override
    public long throttleCount(String resourceId) {
        return metricsListener.throttleCount(resourceId);
    }
}
