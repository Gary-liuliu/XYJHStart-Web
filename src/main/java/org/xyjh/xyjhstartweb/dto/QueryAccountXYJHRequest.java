package org.xyjh.xyjhstartweb.dto;

import lombok.Data;

/**
 * 查询XYJH账号请求DTO
 */
@Data
public class QueryAccountXYJHRequest extends PageRequest {
    private String account;
    private String accountName;
    private Integer status;
    private String remark;
    private String strongCharacter;
}