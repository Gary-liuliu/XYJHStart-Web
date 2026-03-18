package org.xyjh.xyjhstartweb.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xyjh.xyjhstartweb.dto.ActivationRequest;
import org.xyjh.xyjhstartweb.dto.AppConfigRequest;
import org.xyjh.xyjhstartweb.dto.Role;
import org.xyjh.xyjhstartweb.entity.AppConfigItem;
import org.xyjh.xyjhstartweb.mapper.LicenseKeyMapper;
import org.xyjh.xyjhstartweb.service.AccountInfoService;
import org.xyjh.xyjhstartweb.service.LicenseService;
import org.xyjh.xyjhstartweb.util.Result;

import java.util.List;

@RestController
@RequestMapping("/api/license") // 为所有与许可证相关的API设置一个统一的基础路径
public class LicenseController {

    private final LicenseService licenseService;

    private static final Logger log = LoggerFactory.getLogger(AccountInfoService.class);

    // 推荐使用构造函数注入，而不是字段注入
    @Autowired
    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    /**
     * [已重构] 客户端提交激活申请接口
     * @param request 包含 licenseKey 和 deviceId 的请求体
     * @return 返回包含JWT的激活结果
     */
    @PostMapping("/activate")
    public Result<String> submitActivation(@RequestBody ActivationRequest request) {
        log.info("接收到激活申请, Key: {}, Device ID: {}", request.getLicenseKey(), request.getDeviceId());
        return licenseService.submitActivationRequest(request);
    }
    /**
     * [新] 客户端轮询激活状态接口
     */
    @GetMapping("/status")
    public Result<String> checkStatus(@RequestParam String licenseKey, @RequestParam String deviceId) {
        return licenseService.checkActivationStatus(licenseKey, deviceId);
    }
    /**
     * 许可证状态验证接口
     * @param token 从请求头中获取的 JWT
     * @return 返回验证结果
     */
    @GetMapping("/validate")
    public Result<Void> validate(@RequestHeader("Authorization") String token) {
        // Authorization 请求头通常格式为 "Bearer eyJhbGciOiJIUzI1NiJ9..."
        // 我们需要去掉 "Bearer " 前缀
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            return licenseService.validateToken(jwt);
        }
        return Result.fail(400, "无效的请求头"); // 400 Bad Request
    }

    /**
     * 获取所有角色数据
     * GET /api/roles
     */
    @GetMapping("/roles")
    public Result<List<Role>> getAllRoles() {
        try {
            List<Role> roles = licenseService.selectAllRoles();
            return Result.success(roles);
        } catch (Exception e) {
            // 记录日志并返回错误
            return Result.fail(500, "获取角色列表失败");
        }
    }

    @GetMapping("/rolenames")
    public Result<List<Role>> getAllRoleNames() {
        try {
            List<Role> roles = licenseService.selectAllRoles();
            return Result.success(roles);
        } catch (Exception e) {
            // 记录日志并返回错误
            return Result.fail(500, "获取角色名字列表失败");
        }
    }

    /**
     * [新增] 根据配置类型查询最近的 10 条记录
     * GET /api/license/configs/recent?type=1
     * @param type 配置类型：1 兑换码，2 活动代码
     * @return 返回配置项列表
     */
    @GetMapping("/configs/recent")
    public Result<List<AppConfigItem>> getRecentConfigs(@RequestParam Integer type) {
        return licenseService.getRecentConfigsByType(type);
    }

    /**
     * [新增] 新增一条配置记录
     * POST /api/license/configs
     * @param request 包含配置类型和配置值的请求体
     * @return 返回新增结果
     */
    @PostMapping("/configs")
    public Result<AppConfigItem> addConfig(@RequestBody AppConfigRequest request) {
        return licenseService.addConfig(request);
    }
}