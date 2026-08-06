package com.example.ratelimiter.event;

import com.example.ratelimiter.config.RateLimiterAlgorithm;
import com.example.ratelimiter.core.RateLimitResult;
import org.springframework.context.ApplicationEvent;

/**
 * Observer pattern, implemented on top of Spring's {@code ApplicationEvent}
 * publish/subscribe mechanism: published once per throttled request so any
 * number of independent listeners (metrics, alerting, audit logging) can
 * react without the service layer knowing they exist.
 */
public class RateLimitExceededEvent extends ApplicationEvent {

    private final String resourceId;
    private final String clientKey;
    private final RateLimiterAlgorithm algorithm;
    private final RateLimitResult result;

    public RateLimitExceededEvent(String resourceId, String clientKey, RateLimiterAlgorithm algorithm, RateLimitResult result) {
        super(resourceId);
        this.resourceId = resourceId;
        this.clientKey = clientKey;
        this.algorithm = algorithm;
        this.result = result;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getClientKey() {
        return clientKey;
    }

    public RateLimiterAlgorithm getAlgorithm() {
        return algorithm;
    }

    public RateLimitResult getResult() {
        return result;
    }
}
