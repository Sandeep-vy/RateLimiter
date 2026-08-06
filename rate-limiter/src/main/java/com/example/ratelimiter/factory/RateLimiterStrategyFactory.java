package com.example.ratelimiter.factory;

import com.example.ratelimiter.algorithm.RateLimiterStrategy;
import com.example.ratelimiter.config.RateLimiterAlgorithm;
import com.example.ratelimiter.exception.UnsupportedAlgorithmException;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory pattern for {@link RateLimiterStrategy} instances, resolved by
 * {@link RateLimiterAlgorithm}. Spring injects every {@code RateLimiterStrategy}
 * bean present in the context; the factory just indexes them by the
 * algorithm they declare. This is the Open/Closed seam of the whole system:
 * adding a sixth algorithm means writing one new {@code @Component} class
 * that implements {@code RateLimiterStrategy} - this factory, the registry,
 * and the service layer need zero changes.
 */
@Component
public class RateLimiterStrategyFactory {

    private final Map<RateLimiterAlgorithm, RateLimiterStrategy> strategiesByAlgorithm;

    public RateLimiterStrategyFactory(List<RateLimiterStrategy> strategies) {
        this.strategiesByAlgorithm = strategies.stream()
                .collect(Collectors.toUnmodifiableMap(RateLimiterStrategy::getAlgorithm, Function.identity()));
    }

    public RateLimiterStrategy create(RateLimiterAlgorithm algorithm) {
        RateLimiterStrategy strategy = strategiesByAlgorithm.get(algorithm);
        if (strategy == null) {
            throw new UnsupportedAlgorithmException(algorithm);
        }
        return strategy;
    }

    public Set<RateLimiterAlgorithm> supportedAlgorithms() {
        return EnumSet.copyOf(strategiesByAlgorithm.keySet());
    }
}
