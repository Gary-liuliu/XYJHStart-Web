package org.xyjh.xyjhstartweb.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xyjh.xyjhstartweb.dto.AdminLoginRequest;
import org.xyjh.xyjhstartweb.dto.ChangePasswordRequest;
import org.xyjh.xyjhstartweb.dto.CreateAdminRequest;
import org.xyjh.xyjhstartweb.entity.AdminUser;
import org.xyjh.xyjhstartweb.mapper.AdminUserMapper;
import org.xyjh.xyjhstartweb.util.Result;
import org.xyjh.xyjhstartweb.util.jwt.JwtUtil;
import java.util.List;
import java.util.Date;

/**
 * 专门负责处理所有管理员账户相关业务逻辑的服务层
 */
@Service
@Slf4j
public class AdminUserService {

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil; // 登录需要 JWT 工具

    private static final Logger log = LoggerFactory.getLogger(AccountInfoService.class);

    public AdminUserService(AdminUserMapper adminUserMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.adminUserMapper = adminUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    /**
     * [管理员] 处理登录请求
     */
    public Result<String> adminLogin(AdminLoginRequest request) {
        AdminUser admin = adminUserMapper.findByUsername(request.getUsername());
        if (admin != null && passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            Date expiration = new Date(System.currentTimeMillis() + 8 * 60 * 60 * 1000); // 8小时过期

            // 【修改点】调用新的、正确的方法来生成管理员Token
            String adminToken = jwtUtil.generateAdminToken(admin.getUsername(), expiration);

            log.info("管理员 '{}' 登录成功。", admin.getUsername());
            return Result.success("登录成功", adminToken);
        } else {
            log.warn("管理员登录失败，用户: {}", request.getUsername());
            return Result.fail(401, "用户名或密码错误");
        }
    }

    /**
     * 修改当前登录管理员的密码
     */
    public Result<Void> changeCurrentUserPassword(ChangePasswordRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        AdminUser currentUser = adminUserMapper.findByUsername(currentUsername);
        if (currentUser == null) {
            return Result.fail(404, "当前用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            return Result.fail(400, "旧密码错误");
        }
        String newHashedPassword = passwordEncoder.encode(request.getNewPassword());
        adminUserMapper.updatePassword(currentUsername, newHashedPassword);
        log.info("管理员 '{}' 成功修改了自己的密码。", currentUsername);
        return Result.success("密码修改成功", null);
    }

    /**
     * 创建一个新的管理员账号
     */
    @Transactional
    public Result<AdminUser> createAdminUser(CreateAdminRequest request) {
        if (adminUserMapper.findByUsername(request.getUsername()) != null) {
            return Result.fail(409, "用户名已存在");
        }
        AdminUser newUser = new AdminUser();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(request.getRole());
        adminUserMapper.insert(newUser);
        log.info("管理员 '{}' 创建了新的管理员账号 '{}'。",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                request.getUsername());
        newUser.setPassword(null);
        return Result.success("新管理员创建成功", newUser);
    }

    /**
     * 删除一个管理员账号
     */
    @Transactional
    public Result<Void> deleteAdminUser(Long idToDelete) {
        if (adminUserMapper.count() <= 1) {
            return Result.fail(400, "无法删除最后一个管理员账户");
        }
        AdminUser userToDelete = adminUserMapper.findById(idToDelete);
        if (userToDelete == null) {
            return Result.fail(404, "要删除的用户不存在");
        }
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (userToDelete.getUsername().equals(currentUsername)) {
            return Result.fail(400, "不能删除自己");
        }
        adminUserMapper.deleteById(idToDelete);
        log.info("管理员 '{}' 删除了管理员账号 '{}' (ID: {})。", currentUsername, userToDelete.getUsername(), idToDelete);
        return Result.success("删除成功", null);
    }

    /**
     * [新增] 获取所有管理员账户列表
     */
    public Result<List<AdminUser>> getAllAdminUsers() {
        try {
            List<AdminUser> users = adminUserMapper.findAll();

            // 【安全】: 移除所有密码信息，防止泄露到前端
            for (AdminUser user : users) {
                user.setPassword(null);
            }

            return Result.success("查询成功", users);
        } catch (Exception e) {
            log.error("查询所有管理员失败", e);
            return Result.fail(500, "服务器错误，无法查询管理员列表");
        }
    }
}