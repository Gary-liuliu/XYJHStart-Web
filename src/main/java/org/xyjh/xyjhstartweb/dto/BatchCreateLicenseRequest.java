package org.xyjh.xyjhstartweb.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 批量创建许可证密钥时使用的请求体 DTO
 */
@Data
public class BatchCreateLicenseRequest {

    /**
     * 需要生成的许可证数量
     */
    @Min(value = 1, message = "一次至少需要生成1个密钥")
    @Max(value = 100, message = "一次最多只能生成100个密钥，如需更多请分批操作")
    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}