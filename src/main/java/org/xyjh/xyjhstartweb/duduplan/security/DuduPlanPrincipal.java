package org.xyjh.xyjhstartweb.duduplan.security;

public record DuduPlanPrincipal(DuduPlanRole role, long accessTokenExpiresAt) {
}
