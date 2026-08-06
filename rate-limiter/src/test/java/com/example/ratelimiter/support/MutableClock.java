package com.example.ratelimiter.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Test double for {@link Clock} that only advances when told to, so window
 * rollover / refill / leak math can be exercised deterministically instead
 * of relying on real sleeps.
 */
public final class MutableClock extends Clock {

    private Instant instant;
    private final ZoneId zone;

    private MutableClock(Instant instant, ZoneId zone) {
        this.instant = instant;
        this.zone = zone;
    }

    public static MutableClock startingAt(Instant instant) {
        return new MutableClock(instant, ZoneId.of("UTC"));
    }

    public void advance(Duration duration) {
        this.instant = this.instant.plus(duration);
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableClock(instant, zone);
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
