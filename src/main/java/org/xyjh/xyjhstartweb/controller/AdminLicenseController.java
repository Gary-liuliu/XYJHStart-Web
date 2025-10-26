package org.xyjh.xyjhstartweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xyjh.xyjhstartweb.dto.ApproveLicenseRequest;
import org.xyjh.xyjhstartweb.dto.CreateLicenseKeyRequest;
import org.xyjh.xyjhstartweb.dto.PagedResult;
import org.xyjh.xyjhstartweb.entity.LicenseKey;
import org.xyjh.xyjhstartweb.service.LicenseService;
import org.xyjh.xyjhstartweb.util.Result;

import java.util.List;

/**
 * 许可证密钥后台管理 API 控制器
 * 注意：在生产环境中，此控制器下的所有接口都应受到严格的权限保护，只允许管理员访问。
 */
@RestController
@RequestMapping("/api/admin/licenses") // 使用独立的、有 admin 标识的路径
public class AdminLicenseController {

    private final LicenseService licenseService;

    @Autowired
    public AdminLicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    /**
     * [修改] 分页获取所有许可证密钥列表
     * @param page 请求的页码 (从 0 开始)，默认为 0
     * @param size 每页的数量，默认为 10
     * @return 包含分页信息的密钥列表
     */
    @GetMapping
    public Result<PagedResult<LicenseKey>> getAllLicenseKeysPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return licenseService.getAllKeysPaginated(page, size);
    }
    /**
     * [新增] 分页获取所有已激活的许可证密钥列表
     * @param page 请求的页码 (从 0 开始)，默认为 0
     * @param size 每页的数量，默认为 10
     * @return 包含分页信息的已激活密钥列表
     */
    @GetMapping("/activated")
    public Result<PagedResult<LicenseKey>> getActivatedLicenseKeysPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return licenseService.getActivatedKeysPaginated(page, size);
    }
    /**
     * 创建一个新的许可证密钥
     * @param request 包含客户备注等信息的请求体
     * @return 返回新创建的许可证密钥的完整信息
     */
    @PostMapping
    public Result<LicenseKey> createLicenseKey(@RequestBody CreateLicenseKeyRequest request) {
        return licenseService.generateAndSaveKey(request);
    }

    /**
     * 吊销指定的许可证密钥
     * @param id 路径变量，代表要吊销的密钥的 ID
     * @return 返回操作结果
     */
    @PutMapping("/{id}/revoke") // 使用 PUT 方法，表示更新资源状态，符合 RESTful 风格
    public Result<Void> revokeLicenseKey(@PathVariable Long id) {
        return licenseService.revokeKey(id);
    }

    /**
     * [新] 获取所有待审批的许可证列表
     */
    @GetMapping("/pending")
    public Result<List<LicenseKey>> getPendingLicenseKeys() {
        return licenseService.getPendingKeys();
    }

    /**
     * [新] 批准一个待审批的许可证
     * @param id 要批准的密钥ID
     * @param request 包含授权天数的请求体
     */
    @PutMapping("/{id}/approve")
    public Result<LicenseKey> approveLicenseKey(@PathVariable Long id, @RequestBody ApproveLicenseRequest request) {
        return licenseService.approveKey(id, request);
    }
}