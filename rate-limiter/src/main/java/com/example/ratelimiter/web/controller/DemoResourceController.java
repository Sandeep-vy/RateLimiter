package com.example.ratelimiter.web.controller;

import com.example.ratelimiter.web.annotation.RateLimited;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * A sample protected endpoint demonstrating {@link RateLimited} in use. The
 * resource id "demo-api" is bootstrapped on startup (see
 * {@code RateLimiterBootstrap}) and can be reconfigured live via the admin
 * controller while requests keep flowing.
 */
@RestController
public class DemoResourceController {

    @RateLimited(resource = "demo-api")
    @GetMapping("/api/demo/ping")
    public Map<String, Object> ping() {
        return Map.of("message", "pong", "timestamp", Instant.now());
    }
}
