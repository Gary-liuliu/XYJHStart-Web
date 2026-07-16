package org.xyjh.xyjhstartweb.duduplan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "dudu-plan")
public class DuduPlanProperties {
    private String ownerPasswordHash = "";
    private String observerPasswordHash = "";
    private String tokenSecret = "";

    public String getOwnerPasswordHash() {
        return ownerPasswordHash;
    }

    public void setOwnerPasswordHash(String ownerPasswordHash) {
        this.ownerPasswordHash = ownerPasswordHash;
    }

    public String getObserverPasswordHash() {
        return observerPasswordHash;
    }

    public void setObserverPasswordHash(String observerPasswordHash) {
        this.observerPasswordHash = observerPasswordHash;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }
}
