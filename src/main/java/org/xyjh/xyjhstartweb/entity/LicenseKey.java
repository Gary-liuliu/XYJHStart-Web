package org.xyjh.xyjhstartweb.entity;

import lombok.Data;
import java.time.LocalDateTime;
//映射数据库中的 license_key 表
@Data // 使用 Lombok 自动生成 Getter, Setter, etc.
public class LicenseKey {
    private Long id;
    private String licenseKey;
    private String status;
    private String deviceId;
    private LocalDateTime activationDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String tipCustomer;
    private LocalDateTime expirationDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public LocalDateTime getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(LocalDateTime activationDate) {
        this.activationDate = activationDate;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getTipCustomer() {
        return tipCustomer;
    }

    public void setTipCustomer(String tipCustomer) {
        this.tipCustomer = tipCustomer;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }
}