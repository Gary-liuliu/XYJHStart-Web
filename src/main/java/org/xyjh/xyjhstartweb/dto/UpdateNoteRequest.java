package org.xyjh.xyjhstartweb.dto;

import lombok.Data;

/**
 * 单独更新许可证备注时使用的请求体 DTO
 */
@Data
public class UpdateNoteRequest {

    /**
     * 新的客户信息备注。
     * (注意: 如果前端传 null, 备注将被清空)
     */
    private String tipCustomer;

    public String getTipCustomer() {
        return tipCustomer;
    }

    public void setTipCustomer(String tipCustomer) {
        this.tipCustomer = tipCustomer;
    }
}