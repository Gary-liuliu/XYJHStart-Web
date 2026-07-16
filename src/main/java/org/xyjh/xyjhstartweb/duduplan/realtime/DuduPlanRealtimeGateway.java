package org.xyjh.xyjhstartweb.duduplan.realtime;

import org.xyjh.xyjhstartweb.duduplan.security.DuduPlanRole;

import java.util.Map;

public interface DuduPlanRealtimeGateway {
    void sendToRole(DuduPlanRole role, Map<String, Object> event);
    boolean isOnline(DuduPlanRole role);
}
