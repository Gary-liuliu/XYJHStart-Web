package org.xyjh.xyjhstartweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xyjh.xyjhstartweb.dto.AdminLoginRequest;
import org.xyjh.xyjhstartweb.dto.ChangePasswordRequest;
import org.xyjh.xyjhstartweb.dto.CreateAdminRequest;
import org.xyjh.xyjhstartweb.entity.AdminUser;
import org.xyjh.xyjhstartweb.service.AdminUserService;
import org.xyjh.xyjhstartweb.util.Result;
import java.util.List;
/**
 * 管理员账户管理 API 控制器
 */
@RestController
@RequestMapping("/api/admin/users") // 使用专门的用户管理路径
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Autowired
    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * 管理员登录接口
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody AdminLoginRequest request) {
        // 调用新服务中的登录方法
        return adminUserService.adminLogin(request);
    }

    /**
     * [管理员] 创建一个新的管理员账号
     */
    @PostMapping
    public Result<AdminUser> createAdmin(@RequestBody CreateAdminRequest request) {
        return adminUserService.createAdminUser(request);
    }

    /**
     * [管理员] 删除一个管理员账号
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAdmin(@PathVariable Long id) {
        return adminUserService.deleteAdminUser(id);
    }

    /**
     * [管理员] 修改当前登录管理员自己的密码
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        return adminUserService.changeCurrentUserPassword(request);
    }
    /**
     * [新增] 获取所有管理员账户列表
     */
    @GetMapping
    public Result<List<AdminUser>> getAllAdmins() {
        return adminUserService.getAllAdminUsers();
    }
}