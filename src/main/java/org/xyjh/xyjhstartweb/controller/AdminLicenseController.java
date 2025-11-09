package org.xyjh.xyjhstartweb.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xyjh.xyjhstartweb.dto.*;
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

    /**
     * 管理员登录接口
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody AdminLoginRequest request) {
        return licenseService.adminLogin(request);
    }

    /**
     * [新增] 批量创建新的许可证密钥
     * @param request 包含要生成数量的请求体
     * @return 返回新创建的许可证密钥列表
     */
    @PostMapping("/batch")
    public Result<List<LicenseKey>> createLicenseKeysBatch(@Valid @RequestBody BatchCreateLicenseRequest request) {
        return licenseService.generateAndSaveKeysBatch(request);
    }

    /**
     * [新增] 为指定的许可证续期
     * @param id 路径变量，代表要续期的密钥的 ID
     * @param request 包含续期天数的请求体
     * @return 返回更新后的许可证信息
     */

    @PutMapping("/{id}/renew")
    public Result<LicenseKey> renewLicenseKey(@PathVariable Long id, @Valid @RequestBody RenewLicenseRequest request) {
        return licenseService.renewKey(id, request);
    }
    /**
     * [新增] 分页获取所有待审批的许可证密钥列表
     * @param page 请求的页码 (从 0 开始)，默认为 0
     * @param size 每页的数量，默认为 10
     * @return 包含分页信息的待审批密钥列表
     */
    @GetMapping("/pending/paginated") // 使用 /pending/paginated 以区别于原有的 /pending 接口
    public Result<PagedResult<LicenseKey>> getPendingLicenseKeysPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return licenseService.getPendingKeysPaginated(page, size);
    }

    /**
     * [新增] 分页获取所有已过期的许可证密钥列表
     * @param page 请求的页码 (从 0 开始)，默认为 0
     * @param size 每页的数量，默认为 10
     * @return 包含分页信息的已过期密钥列表
     */
    @GetMapping("/expired")
    public Result<PagedResult<LicenseKey>> getExpiredLicenseKeysPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return licenseService.getExpiredKeysPaginated(page, size);
    }
    /**
     * [新增] 单独更新一个许可证的备注
     * @param id 路径变量，代表要更新的密钥 ID
     * @param request 包含新备注的请求体
     * @return 返回更新后的许可证信息
     */
    @PutMapping("/{id}/note") // 使用 /note 路径
    public Result<LicenseKey> updateLicenseNote(@PathVariable Long id, @RequestBody UpdateNoteRequest request) {
        return licenseService.updateKeyNote(id, request);
    }

}