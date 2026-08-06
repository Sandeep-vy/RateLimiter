package com.example.ratelimiter.web.interceptor;

import com.example.ratelimiter.core.RateLimitResult;
import com.example.ratelimiter.exception.RateLimitExceededException;
import com.example.ratelimiter.service.RateLimitService;
import com.example.ratelimiter.web.annotation.RateLimited;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Enforces {@link RateLimited} on controller methods. Deliberately thin: it
 * only resolves the client key and delegates the actual decision to
 * {@link RateLimitService}, so the HTTP concerns (headers, status codes)
 * stay separate from the rate-limiting domain logic.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RateLimited annotation = handlerMethod.getMethodAnnotation(RateLimited.class);
        if (annotation == null) {
            return true;
        }

        String clientKey = resolveClientKey(request, annotation.keyHeader());
        RateLimitResult result = rateLimitService.tryAcquire(annotation.resource(), clientKey);

        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remainingPermits()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(result.resetAt().getEpochSecond()));

        if (!result.allowed()) {
            throw new RateLimitExceededException(annotation.resource(), result);
        }
        return true;
    }

    private String resolveClientKey(HttpServletRequest request, String headerName) {
        String header = request.getHeader(headerName);
        return (header != null && !header.isBlank()) ? header : request.getRemoteAddr();
    }
}
