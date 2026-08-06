package com.example.ratelimiter.exception;

public class UnknownResourceException extends RuntimeException {

    public UnknownResourceException(String resourceId) {
        super("No rate-limited resource registered with id: " + resourceId);
    }
}
