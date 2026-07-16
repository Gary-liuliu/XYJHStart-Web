package org.xyjh.xyjhstartweb.dto;

import lombok.Data;

@Data
public class SyncAccountXYJHRequest {
    private String accountName;
    private String account;
    private String password;
    private Integer greenTicket;
    private Integer yellowTicket;
    private String remark;
}
