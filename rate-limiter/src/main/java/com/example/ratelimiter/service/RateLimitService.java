package com.example.ratelimiter.service;

import com.example.ratelimiter.config.RateLimitConfig;
import com.example.ratelimiter.core.RateLimitResult;
import com.example.ratelimiter.core.RateLimiter;

/**
 * Facade over the registry/factory/strategy machinery: this is the one type
 * the web layer (and any future non-HTTP caller) depends on. It hides how
 * resources are registered, how algorithms are resolved, and how throttle
 * events get published.
 */
public interface RateLimitService {

    RateLimitResult tryAcquire(String resourceId, String clientKey);

    RateLimiter registerOrReconfigure(String resourceId, RateLimitConfig config);

    RateLimiter getResource(String resourceId);

    boolean isRegistered(String resourceId);

    long throttleCount(String resourceId);
}
