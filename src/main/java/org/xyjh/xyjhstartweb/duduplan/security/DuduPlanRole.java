package org.xyjh.xyjhstartweb.duduplan.security;

public enum DuduPlanRole {
    OWNER("owner", "嘟嘟"),
    OBSERVER("observer", "肚肚");

    private final String value;
    private final String accountName;

    DuduPlanRole(String value, String accountName) {
        this.value = value;
        this.accountName = accountName;
    }

    public String value() {
        return value;
    }

    public String accountName() {
        return accountName;
    }

    public DuduPlanRole peer() {
        return this == OWNER ? OBSERVER : OWNER;
    }

    public static DuduPlanRole fromValue(String value) {
        for (DuduPlanRole role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown Dudu Plan role");
    }

    public static DuduPlanRole fromAccountName(String accountName) {
        for (DuduPlanRole role : values()) {
            if (role.accountName.equals(accountName)) {
                return role;
            }
        }
        return null;
    }
}
