package com.example.ratelimiter.exception;

import com.example.ratelimiter.config.RateLimiterAlgorithm;

public class UnsupportedAlgorithmException extends RuntimeException {

    public UnsupportedAlgorithmException(RateLimiterAlgorithm algorithm) {
        super("No RateLimiterStrategy bean registered for algorithm: " + algorithm);
    }
}
