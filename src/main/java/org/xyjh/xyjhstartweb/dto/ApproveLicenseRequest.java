package org.xyjh.xyjhstartweb.dto;

import lombok.Data;

@Data
public class ApproveLicenseRequest {
    // 管理员设定的授权天数
    private int durationInDays;

    public int getDurationInDays() {
        return durationInDays;
    }

    public void setDurationInDays(int durationInDays) {
        this.durationInDays = durationInDays;
    }
}