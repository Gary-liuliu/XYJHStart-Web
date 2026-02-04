package org.xyjh.xyjhstartweb.dto;

import lombok.Data;

/**
 * 管理员创建新管理员时使用的请求体 DTO
 */
@Data
public class CreateAdminRequest {
    // 新管理员的用户名
    private String username;
    // 新管理员的初始密码
    private String password;
    // (可选) 新管理员的角色，默认为 ADMIN
    private String role = "ADMIN";

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}