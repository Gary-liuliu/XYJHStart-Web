package org.xyjh.xyjhstartweb.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xyjh.xyjhstartweb.dto.ActivationRequest;
import org.xyjh.xyjhstartweb.service.AccountInfoService;
import org.xyjh.xyjhstartweb.service.LicenseService;
import org.xyjh.xyjhstartweb.util.Result;

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
}