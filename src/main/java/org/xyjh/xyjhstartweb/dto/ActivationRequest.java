package org.xyjh.xyjhstartweb.dto;

import lombok.Data;
//这个类代表客户端激活时发送过来的 JSON 数据。
@Data
public class ActivationRequest {
    private String licenseKey;
    private String deviceId;

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}