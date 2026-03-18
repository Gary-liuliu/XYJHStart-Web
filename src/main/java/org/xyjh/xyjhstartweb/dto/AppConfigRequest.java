package org.xyjh.xyjhstartweb.dto;

import lombok.Data;

/**
 * 配置项请求 DTO
 */
@Data
public class AppConfigRequest {
    private Integer configType; // 1 兑换码，2 活动代码
    private String configValue;
}
