package org.xyjh.xyjhstartweb.duduplan.service;

import org.springframework.stereotype.Component;
import org.xyjh.xyjhstartweb.duduplan.security.DuduPlanRole;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class DuduPlanEncouragementRateLimiter {
    private static final long COOLDOWN_MILLIS = 3_000L;
    private final ConcurrentHashMap<DuduPlanRole, Long> lastAcceptedAt = new ConcurrentHashMap<>();

    public boolean tryAcquire(DuduPlanRole role, long now) {
        return lastAcceptedAt.compute(role, (ignored, previous) ->
                previous == null || now - previous >= COOLDOWN_MILLIS ? now : previous) == now;
    }
}
