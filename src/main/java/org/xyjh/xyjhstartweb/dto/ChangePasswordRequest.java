package org.xyjh.xyjhstartweb.dto;

import lombok.Data;

/**
 * 管理员修改自己密码时使用的请求体 DTO
 */
@Data
public class ChangePasswordRequest {
    // 旧密码，用于验证身份
    private String oldPassword;
    // 新密码
    private String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}