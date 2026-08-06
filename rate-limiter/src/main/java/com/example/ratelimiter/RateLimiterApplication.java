package com.example.ratelimiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class RateLimiterApplication {

    public static void main(String[] args) {
        SpringApplication.run(RateLimiterApplication.class, args);
    }

    /**
     * Single shared {@link Clock} bean injected into every algorithm
     * strategy. Centralizing time behind this abstraction (rather than each
     * strategy calling {@code System.currentTimeMillis()} directly) is what
     * lets unit tests substitute {@code Clock.fixed(...)} and deterministically
     * exercise refill/leak/window-rollover logic without real sleeps.
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
