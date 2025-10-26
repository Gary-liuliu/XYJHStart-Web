package org.xyjh.xyjhstartweb.dto;

import lombok.Data;

/**
 * 创建新许可证密钥时使用的请求体 DTO (Data Transfer Object)
 */
@Data
public class CreateLicenseKeyRequest {
    // 客户信息备注
    private String tipCustomer;
    // (可选) 可以增加一个字段用于批量生成
    private int count = 1;
}