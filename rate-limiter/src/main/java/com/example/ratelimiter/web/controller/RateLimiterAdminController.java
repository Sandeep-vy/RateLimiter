package com.example.ratelimiter.web.controller;

import com.example.ratelimiter.config.RateLimitConfigFactory;
import com.example.ratelimiter.config.RateLimiterAlgorithm;
import com.example.ratelimiter.core.RateLimiter;
import com.example.ratelimiter.factory.RateLimiterStrategyFactory;
import com.example.ratelimiter.service.RateLimitService;
import com.example.ratelimiter.web.dto.RateLimitConfigRequest;
import com.example.ratelimiter.web.dto.RateLimiterStatusResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * Operational control-plane for rate limiting: register a resource under a
 * chosen algorithm, and - the central requirement of this exercise - swap
 * that algorithm (and its parameters) for a running resource at any time via
 * the same idempotent endpoint, with zero downtime and no restart.
 */
@RestController
@RequestMapping("/api/admin/rate-limiters")
public class RateLimiterAdminController {

    private final RateLimitService rateLimitService;
    private final RateLimitConfigFactory configFactory;
    private final RateLimiterStrategyFactory strategyFactory;

    public RateLimiterAdminController(RateLimitService rateLimitService,
                                       RateLimitConfigFactory configFactory,
                                       RateLimiterStrategyFactory strategyFactory) {
        this.rateLimitService = rateLimitService;
        this.configFactory = configFactory;
        this.strategyFactory = strategyFactory;
    }

    @GetMapping("/algorithms")
    public Set<RateLimiterAlgorithm> supportedAlgorithms() {
        return strategyFactory.supportedAlgorithms();
    }

    /** Registers a new resource, or reconfigures/switches the algorithm of an existing one. */
    @PutMapping("/{resourceId}")
    public ResponseEntity<RateLimiterStatusResponse> registerOrSwitch(
            @PathVariable String resourceId, @Valid @RequestBody RateLimitConfigRequest request) {
        RateLimiter limiter = rateLimitService.registerOrReconfigure(resourceId, configFactory.from(request));
        return ResponseEntity.ok(RateLimiterStatusResponse.from(limiter, rateLimitService.throttleCount(resourceId)));
    }

    @GetMapping("/{resourceId}")
    public RateLimiterStatusResponse status(@PathVariable String resourceId) {
        RateLimiter limiter = rateLimitService.getResource(resourceId);
        return RateLimiterStatusResponse.from(limiter, rateLimitService.throttleCount(resourceId));
    }
}
