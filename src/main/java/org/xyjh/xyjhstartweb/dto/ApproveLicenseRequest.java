package org.xyjh.xyjhstartweb.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
@Data
public class ApproveLicenseRequest {
    // 管理员设定的授权天数
    @Min(value = 1, message = "授权天数必须大于0")
    private int durationInDays;

    public int getDurationInDays() {
        return durationInDays;
    }

    public void setDurationInDays(int durationInDays) {
        this.durationInDays = durationInDays;
    }
    // 客户信息备注 (可选)
    private String tipCustomer;

    public String getTipCustomer() {
        return tipCustomer;
    }

    public void setTipCustomer(String tipCustomer) {
        this.tipCustomer = tipCustomer;
    }
}