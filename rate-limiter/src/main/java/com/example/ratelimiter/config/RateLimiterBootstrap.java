package com.example.ratelimiter.config;

import com.example.ratelimiter.service.RateLimitService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Registers the demo resource on startup so {@code /api/demo/ping} is
 * protected from the very first request. In a real system this would instead
 * read resource definitions from configuration or a database; here it is
 * kept inline to keep the demo self-contained.
 */
@Component
public class RateLimiterBootstrap implements ApplicationRunner {

    private final RateLimitService rateLimitService;

    public RateLimiterBootstrap(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public void run(ApplicationArguments args) {
        rateLimitService.registerOrReconfigure("demo-api", TokenBucketConfig.builder()
                .capacity(5)
                .refillTokens(5)
                .refillPeriod(Duration.ofSeconds(10))
                .build());
    }
}
