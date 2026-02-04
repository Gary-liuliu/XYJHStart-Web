package org.xyjh.xyjhstartweb.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 管理员续期或更新许可证时使用的请求体 DTO
 */
@Data
public class RenewLicenseRequest {

    /**
     * 续期天数（可选）
     * 如果提供此字段，将计算新的过期时间。
     */
    @Min(value = 1, message = "续期天数必须大于0")
    private Integer durationInDays; // 改为 Integer 以允许 null

    /**
     * 客户信息备注（可选）
     * 如果提供此字段，将覆盖旧的备注。
     */
    private String tipCustomer;

    public Integer getDurationInDays() {
        return durationInDays;
    }

    public void setDurationInDays(Integer durationInDays) {
        this.durationInDays = durationInDays;
    }

    public String getTipCustomer() {
        return tipCustomer;
    }

    public void setTipCustomer(String tipCustomer) {
        this.tipCustomer = tipCustomer;
    }
}