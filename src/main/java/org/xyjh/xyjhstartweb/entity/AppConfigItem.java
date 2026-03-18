package org.xyjh.xyjhstartweb.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 配置表实体类
 */
@Data
public class AppConfigItem {
    private Long id;
    private Integer configType; // 1 兑换码，2 活动代码
    private String configValue;
    private LocalDateTime createdAt;
}
