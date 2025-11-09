package org.xyjh.xyjhstartweb.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminUser {
    private Long id;
    private String username;
    private String password;
    private String role;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}