package org.xyjh.xyjhstartweb.duduplan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "dudu-plan")
public class DuduPlanProperties {
    private String ownerPassword = "";
    private String observerPassword = "";
    private String tokenSecret = "";

    public String getOwnerPassword() {
        return ownerPassword;
    }

    public void setOwnerPassword(String ownerPassword) {
        this.ownerPassword = ownerPassword;
    }

    public String getObserverPassword() {
        return observerPassword;
    }

    public void setObserverPassword(String observerPassword) {
        this.observerPassword = observerPassword;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }
}
