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
}