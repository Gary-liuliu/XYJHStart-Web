package org.xyjh.xyjhstartweb.duduplan.security;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DuduPlanLoginRateLimiter {
    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, AttemptState> attempts = new ConcurrentHashMap<>();
    private final Clock clock = Clock.systemUTC();

    public boolean isBlocked(String key) {
        Instant now = clock.instant();
        AttemptState state = attempts.get(key);
        if (state == null) {
            return false;
        }
        if (state.blockedUntil != null && state.blockedUntil.isAfter(now)) {
            return true;
        }
        if (state.firstFailure.plus(WINDOW).isBefore(now)) {
            attempts.remove(key, state);
        }
        return false;
    }

    public void recordFailure(String key) {
        Instant now = clock.instant();
        attempts.compute(key, (ignored, current) -> {
            if (current == null || current.firstFailure.plus(WINDOW).isBefore(now)) {
                return new AttemptState(1, now, null);
            }
            int failures = current.failures + 1;
            Instant blockedUntil = failures >= MAX_FAILURES ? now.plus(BLOCK_DURATION) : current.blockedUntil;
            return new AttemptState(failures, current.firstFailure, blockedUntil);
        });
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    private record AttemptState(int failures, Instant firstFailure, Instant blockedUntil) {
    }
}
