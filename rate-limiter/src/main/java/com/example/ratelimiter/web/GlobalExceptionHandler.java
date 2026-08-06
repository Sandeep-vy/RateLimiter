package com.example.ratelimiter.web;

import com.example.ratelimiter.exception.RateLimitExceededException;
import com.example.ratelimiter.exception.UnknownResourceException;
import com.example.ratelimiter.exception.UnsupportedAlgorithmException;
import com.example.ratelimiter.web.dto.ApiErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates domain exceptions into HTTP responses in one place, so
 * controllers and the interceptor stay free of status-code decisions.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex) {
        long retryAfterSeconds = Math.max(1, ex.getResult().retryAfterMillis() / 1000);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds))
                .body(ApiErrorResponse.of(429, "Too Many Requests", ex.getMessage()));
    }

    @ExceptionHandler(UnknownResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleUnknownResource(UnknownResourceException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(404, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(UnsupportedAlgorithmException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedAlgorithm(UnsupportedAlgorithmException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(400, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(400, "Bad Request", ex.getMessage()));
    }
}
