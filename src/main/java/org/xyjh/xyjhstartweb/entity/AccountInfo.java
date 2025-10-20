package org.xyjh.xyjhstartweb.entity;

import java.time.LocalDateTime;

public class AccountInfo {
    private Long id;
    private String licenseId;
    private String account;
    private String password;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ===== Getter & Setter =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(String licenseId) {
        this.licenseId = licenseId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    // ===== 可选: toString =====
    @Override
    public String toString() {
        return "AccountInfo{" +
                "id=" + id +
                ", licenseId='" + licenseId + '\'' +
                ", account='" + account + '\'' +
                ", password='" + password + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}