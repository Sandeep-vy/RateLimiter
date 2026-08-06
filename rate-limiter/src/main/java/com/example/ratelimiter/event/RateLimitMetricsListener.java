package com.example.ratelimiter.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Observer: reacts to {@link RateLimitExceededEvent} to maintain a per-resource
 * throttle counter and emit a log line, entirely decoupled from the code path
 * that decided to throttle the request.
 */
@Component
public class RateLimitMetricsListener {

    private static final Logger log = LoggerFactory.getLogger(RateLimitMetricsListener.class);

    private final ConcurrentMap<String, LongAdder> throttleCountsByResource = new ConcurrentHashMap<>();

    @EventListener
    public void onRateLimitExceeded(RateLimitExceededEvent event) {
        throttleCountsByResource.computeIfAbsent(event.getResourceId(), r -> new LongAdder()).increment();
        log.warn("Throttled request: resource={} clientKey={} algorithm={} retryAfterMs={}",
                event.getResourceId(), event.getClientKey(), event.getAlgorithm(), event.getResult().retryAfterMillis());
    }

    public long throttleCount(String resourceId) {
        LongAdder adder = throttleCountsByResource.get(resourceId);
        return adder == null ? 0L : adder.sum();
    }
}
