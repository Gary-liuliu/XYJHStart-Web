package org.xyjh.xyjhstartweb.dto;

import lombok.Data;
//这个类代表客户端激活时发送过来的 JSON 数据。
@Data
public class ActivationRequest {
    private String licenseKey;
    private String deviceId;
}