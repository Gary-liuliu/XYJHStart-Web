package org.xyjh.xyjhstartweb.service;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xyjh.xyjhstartweb.dto.*;
import org.xyjh.xyjhstartweb.entity.LicenseKey;
import org.xyjh.xyjhstartweb.mapper.LicenseKeyMapper;
import org.xyjh.xyjhstartweb.util.Result;
import org.xyjh.xyjhstartweb.util.jwt.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.xyjh.xyjhstartweb.entity.AdminUser;
import org.xyjh.xyjhstartweb.mapper.AdminUserMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.xyjh.xyjhstartweb.dto.PageRequest;
@Service
public class LicenseService {

    @Autowired
    private LicenseKeyMapper licenseKeyMapper;

    @Autowired
    private JwtUtil jwtUtil;
    private static final Logger log = LoggerFactory.getLogger(AccountInfoService.class);

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =================================================================
    // ==                 面向客户端的接口 (Client-Facing)                 ==
    // =================================================================
    /**
     * [重构] 客户端提交激活申请
     * @param request 包含 licenseKey 和 deviceId
     * @return 告知客户端请求已提交
     */
    public Result<String> submitActivationRequest(ActivationRequest request) {
        log.info("接收到激活申请, Key: {}, Device ID: {}", request.getLicenseKey(), request.getDeviceId());

        LicenseKey key = licenseKeyMapper.findByLicenseKey(request.getLicenseKey());

        if (key == null) {
            return Result.fail(404, "许可证密钥不存在");
        }

        if (!"AVAILABLE".equals(key.getStatus())) {
            return Result.fail(409, "此许可证密钥不可用（可能已被使用或正在审核中）");
        }

        // 更新状态为 PENDING_APPROVAL 并绑定 deviceId
        licenseKeyMapper.submitForApproval(request.getLicenseKey(), request.getDeviceId());
        log.info("激活申请提交成功, Key: {} 已进入待审批状态", request.getLicenseKey());

        return Result.success("激活申请已提交，请等待管理员审核", null);
    }

    /**
     * [重大升级] 客户端轮询检查激活状态 (承载过期检查、续期检查)
     *
     * @param licenseKey 客户端用于激活的 key
     * @param deviceId 客户端的设备ID
     * @return 根据不同状态返回结果
     */
    @Transactional // 增加事务支持，因为我们可能需要执行更新操作
    public Result<String> checkActivationStatus(String licenseKey, String deviceId) {
        LicenseKey key = licenseKeyMapper.findByLicenseKey(licenseKey);

        if (key == null) {
            // 404 - 找不到
            return Result.fail(404, "许可证密钥不存在");
        }

        if (key.getDeviceId() == null || !key.getDeviceId().equals(deviceId)) {
            // 401 - 未授权（设备ID不匹配）
            // 注意：允许 deviceId 为 null 的 PENDING_APPROVAL 或 AVAILABLE 状态通过
            if (!"PENDING_APPROVAL".equals(key.getStatus()) && !"AVAILABLE".equals(key.getStatus())) {
                return Result.fail(401, "许可证与设备ID不匹配");
            }
        }

        // -----------------------------------------------------------------
        // 【核心逻辑】实现了你提出的 "检查-更新" 需求
        // 检查许可证是否在数据库中为 ACTIVATED，但实际上已过期
        // (这是为了处理定时任务尚未执行时的 "临界" 状态)
        if ("ACTIVATED".equals(key.getStatus()) &&
                key.getExpirationDate() != null &&
                key.getExpirationDate().isBefore(LocalDateTime.now())) {

            log.warn("许可证 {} (ID: {}) 被客户端检查时发现已过期，服务器状态将从 ACTIVATED 更新为 EXPIRED。", key.getLicenseKey(), key.getId());

            // 1. 执行你要求的更新操作
            licenseKeyMapper.updateStatusById(key.getId(), "EXPIRED");

            // 2. 更新本地对象状态，以便后续 switch 逻辑能正确处理
            key.setStatus("EXPIRED");
        }
        // -----------------------------------------------------------------

        // 使用 switch 处理所有可能的状态，使逻辑更清晰
        switch (key.getStatus()) {
            case "ACTIVATED":
                // 状态为 ACTIVATED 且未过期，说明:
                // 1. 这是一个正常的用户
                // 2. 或者这是一个刚刚续期成功、状态被管理员改回 ACTIVATED 的用户
                // 无论哪种，都返回一个包含最新过期时间的新 Token

                if (key.getExpirationDate() == null) {
                    log.error("数据错误：Key {} (ID: {}) 已激活但没有过期时间！", licenseKey, key.getId());
                    return Result.fail(500, "许可证数据异常，请联系管理员");
                }

                Date expiration = Date.from(key.getExpirationDate().atZone(ZoneId.systemDefault()).toInstant());
                String token = jwtUtil.generateToken(deviceId, licenseKey, expiration);

                // 200 - 激活成功 (或续期成功)
                return Result.success("激活成功", token);

            case "EXPIRED":
                // 状态为 EXPIRED (无论是定时任务更新的，还是本次调用更新的)
                // 这是明确的指引，告知客户端去续期
                // 403 - 禁止访问 (已过期)
                return Result.fail(403, "许可证已过期，请联系管理员续期");

            case "PENDING_APPROVAL":
                // 状态为 PENDING_APPROVAL，告知客户端继续等待
                // 202 - 已接受 (处理中)
                return Result.fail(202, "审核中，请稍后重试");

            case "REVOKED":
                // 状态为 REVOKED，告知客户端许可证已被吊销
                // 403 - 禁止访问 (已吊销)
                return Result.fail(403, "许可证已被吊销，请联系管理员");

            case "AVAILABLE":
                // 状态为 AVAILABLE，说明用户从未提交过激活申请
                // 409 - 冲突 (状态不正确)
                return Result.fail(409, "许可证尚未提交激活申请");

            default:
                // 未知状态
                log.error("许可证 {} (ID: {}) 处于未知的状态: {}", licenseKey, key.getId(), key.getStatus());
                return Result.fail(500, "未知的许可证状态");
        }
    }
    /**
     * 生成并保存一个新的许可证密钥
     * @param request 包含客户备注等信息的请求对象
     * @return 返回生成结果，成功时 data 为新密钥的字符串
     */
    public Result<LicenseKey> generateAndSaveKey(CreateLicenseKeyRequest request) {
        // 1. 生成一个唯一的、格式化的 License Key 字符串
        String newKeyString = "LK-" + java.time.Year.now().getValue() + "-" + UUID.randomUUID().toString().toUpperCase().substring(0, 18);

        // 2. 创建实体对象
        LicenseKey newKey = new LicenseKey();
        newKey.setLicenseKey(newKeyString);
        newKey.setTipCustomer(request.getTipCustomer());

        try {
            // 3. 调用 Mapper 将新密钥存入数据库
            licenseKeyMapper.insert(newKey);
            log.info("成功生成并存储了新的 License Key: {}，客户信息: {}", newKeyString, request.getTipCustomer());
            // 4. 返回包含完整新密钥信息的成功结果
            return Result.success("生成成功", newKey);
        } catch (Exception e) {
            log.error("生成 License Key 时数据库出错", e);
            // 可能是因为生成的 key 碰巧重复了 (概率极低)
            return Result.fail(500, "生成失败，数据库错误");
        }
    }

    /**
     * 获取所有许可证密钥的列表
     * @return 包含所有密钥列表的结果
     */
    public Result<List<LicenseKey>> getAllKeys() {
        try {
            List<LicenseKey> keys = licenseKeyMapper.findAll();
            return Result.success("查询成功", keys);
        } catch (Exception e) {
            log.error("查询所有 License Key 时出错", e);
            return Result.fail(500, "查询失败，服务器错误");
        }
    }
    /**
     * [管理员] 吊销一个许可证密钥
     * @param keyId 要吊销的密钥的数据库主键 ID
     * @return 返回操作结果
     */
    public Result<Void> revokeKey(Long keyId) {
        try {
            // 1. 根据 ID 查找密钥是否存在
            LicenseKey key = licenseKeyMapper.findById(keyId);
            if (key == null) {
                log.warn("吊销失败: ID 为 '{}' 的 License Key 不存在。", keyId);
                return Result.fail(404, "指定的许可证密钥不存在");
            }

            // 2. 如果密钥已经是 REVOKED 状态，则无需重复操作
            if ("REVOKED".equals(key.getStatus())) {
                return Result.success("操作成功，该密钥已被吊销", null);
            }

            // 3. 更新状态和备注
            key.setStatus("REVOKED");
            key.setTipCustomer(key.getTipCustomer() + " [吊销于: " + new Date() + "]"); // 在备注中追加吊销信息

            licenseKeyMapper.update(key);
            log.info("成功吊销了 ID 为 '{}' 的 License Key。", keyId);

            return Result.success("吊销成功", null);

        } catch (Exception e) {
            log.error("吊销 License Key (ID: {}) 时发生错误", keyId, e);
            return Result.fail(500, "操作失败，服务器内部错误");
        }
    }
    /**
     * [客户端] 验证持有的 Token 是否仍然有效
     * @param token 客户端本地存储的 JWT
     * @return 验证结果
     */
    public Result<Void> validateToken(String token) {
        Claims claims = jwtUtil.getClaimsFromToken(token);
        if (claims == null) {
            // 这里包含了过期、签名错误等所有解析失败的情况
            return Result.fail(401, "凭证无效或已过期");
        }
        String licenseKey = claims.getSubject();
        LicenseKey keyInDb = licenseKeyMapper.findByLicenseKey(licenseKey);
        if (keyInDb == null || !"ACTIVATED".equals(keyInDb.getStatus())) {
            return Result.fail(403, "您的许可证已被吊销或已失效");
        }
        return Result.success("凭证有效", null);
    }
    // =================================================================
    // ==                 面向管理员的接口 (Admin-Facing)                  ==
    // =================================================================

    /**
     * [修改] 管理员批准激活申请（并可选添加备注）
     * @param keyId 要批准的密钥ID
     * @param request 包含授权天数和可选的客户备注
     * @return 操作结果
     */
    public Result<LicenseKey> approveKey(Long keyId, @Valid ApproveLicenseRequest request) { // 添加 @Valid
        LicenseKey key = licenseKeyMapper.findById(keyId);
        if (key == null || !"PENDING_APPROVAL".equals(key.getStatus())) {
            return Result.fail(404, "未找到待审批的许可证");
        }

        // --- 1. 计算日期 (与之前相同) ---
        Instant now = Instant.now();
        Instant expirationInstant = now.plus(request.getDurationInDays(), ChronoUnit.DAYS);
        Date activationDate = Date.from(now);
        Date expirationDate = Date.from(expirationInstant);

        // --- 2. 获取可选的备注 ---
        String tipCustomer = request.getTipCustomer(); // (如果没传, 这就是 null)

        // --- 3. 更新数据库 (调用新的 Mapper 方法) ---
        licenseKeyMapper.approve(keyId, activationDate, expirationDate, tipCustomer);

        log.info("管理员已批准 Key ID: {}，授权 {} 天", keyId, request.getDurationInDays());
        if (tipCustomer != null) {
            log.info("  -> 并更新了备注: {}", tipCustomer);
        }

        // 返回更新后的完整信息给前端
        return Result.success("批准成功", licenseKeyMapper.findById(keyId));
    }

    /**
     * [新] 获取所有待审批的许可证列表
     */
    public Result<List<LicenseKey>> getPendingKeys() {
        return Result.success("查询成功", licenseKeyMapper.findByStatus("PENDING_APPROVAL"));
    }

    /**
     * [管理员] 分页获取所有许可证密钥
     * @param pageNumber 页码 (从 0 开始)
     * @param pageSize 每页大小
     * @return 封装好的分页结果
     */
    public Result<PagedResult<LicenseKey>> getAllKeysPaginated(int pageNumber, int pageSize) {
        return getAllKeysPaginatedWithCondition(pageNumber, pageSize, null);
    }

    /**
     * [新增] 管理员分页获取所有许可证密钥（支持 tip_customer 条件查询）
     * @param pageNumber 页码 (从 0 开始)
     * @param pageSize 每页大小
     * @param tipCustomer 客户备注条件（可为空，为空则不进行条件过滤）
     * @return 封装好的分页结果
     */
    public Result<PagedResult<LicenseKey>> getAllKeysPaginatedWithCondition(int pageNumber, int pageSize, String tipCustomer) {
        try {
            // 1. 计算 offset
            int offset = pageNumber * pageSize;

            // 2. 根据是否有条件调用不同的 Mapper 方法
            List<LicenseKey> keys;
            long totalElements;
            
            if (tipCustomer != null && !tipCustomer.trim().isEmpty()) {
                // 有条件查询
                keys = licenseKeyMapper.findAllPaginatedByTipCustomer(pageSize, offset, tipCustomer.trim());
                totalElements = licenseKeyMapper.countAllByTipCustomer(tipCustomer.trim());
                log.info("按条件查询所有 License Key，条件: {}，返回 {} 条记录", tipCustomer.trim(), keys.size());
            } else {
                // 无条件查询
                keys = licenseKeyMapper.findAllPaginated(pageSize, offset);
                totalElements = licenseKeyMapper.countAll();
                log.info("查询所有 License Key，返回 {} 条记录", keys.size());
            }

            // 3. 计算总页数
            int totalPages = (int) Math.ceil((double) totalElements / pageSize);

            // 4. 组装 PagedResult 对象
            PagedResult<LicenseKey> pagedResult = new PagedResult<>(pageNumber, pageSize, totalPages, totalElements, keys);

            return Result.success("查询成功", pagedResult);
        } catch (Exception e) {
            log.error("分页查询所有 License Key 时出错", e);
            return Result.fail(500, "查询失败，服务器错误");
        }
    }

    /**
     * [管理员] 分页获取所有已激活的许可证密钥
     * @param pageNumber 页码 (从 0 开始)
     * @param pageSize 每页大小
     * @return 封装好的分页结果
     */
    public Result<PagedResult<LicenseKey>> getActivatedKeysPaginated(int pageNumber, int pageSize) {
        try {
            int offset = pageNumber * pageSize;
            List<LicenseKey> keys = licenseKeyMapper.findActivatedPaginated(pageSize, offset);
            long totalElements = licenseKeyMapper.countActivated();
            int totalPages = (int) Math.ceil((double) totalElements / pageSize);
            PagedResult<LicenseKey> pagedResult = new PagedResult<>(pageNumber, pageSize, totalPages, totalElements, keys);

            return Result.success("查询成功", pagedResult);
        } catch (Exception e) {
            log.error("分页查询已激活 License Key 时出错", e);
            return Result.fail(500, "查询失败，服务器错误");
        }
    }



    /**
     * [管理员] 处理登录请求，使用数据库和 BCrypt 进行验证
     */
    public Result<String> adminLogin(AdminLoginRequest request) {
        AdminUser admin = adminUserMapper.findByUsername(request.getUsername());

        if (admin != null && passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            Date expiration = new Date(System.currentTimeMillis() + 8 * 60 * 60 * 1000); // 8小时过期

            // 【修改点】同样调用新的、正确的方法来生成管理员Token
            String adminToken = jwtUtil.generateAdminToken(admin.getUsername(), expiration);

            log.info("管理员 '{}' 登录成功。", admin.getUsername());
            return Result.success("登录成功", adminToken);
        } else {
            log.warn("管理员登录失败，用户: {}", request.getUsername());
            return Result.fail(401, "用户名或密码错误");
        }
    }


    /**
     * [新增] 批量生成并保存新的许可证密钥
     * @param request 包含要生成数量的请求对象
     * @return 返回生成结果，成功时 data 为新密钥的列表
     */
    @Transactional // 保证所有密钥要么全部成功，要么全部失败
    public Result<List<LicenseKey>> generateAndSaveKeysBatch(BatchCreateLicenseRequest request) {
        int count = request.getCount();
        List<LicenseKey> keysToInsert = new ArrayList<>(count);

        log.info("开始批量生成 {} 个许可证密钥。", count);

        for (int i = 0; i < count; i++) {
            // 1. 生成唯一的 License Key 字符串
            String newKeyString = "LK-" + java.time.Year.now().getValue() + "-" + UUID.randomUUID().toString().toUpperCase().substring(0, 18);

            // 2. 创建实体对象
            LicenseKey newKey = new LicenseKey();
            newKey.setLicenseKey(newKeyString);
            // tipCustomer 为 null，status 由数据库默认为 'AVAILABLE'
            keysToInsert.add(newKey);
        }

        try {
            // 3. 调用 Mapper 批量插入数据库
            if (!keysToInsert.isEmpty()) {
                licenseKeyMapper.batchInsert(keysToInsert);
                log.info("成功批量生成并存储了 {} 个新的 License Key。", count);
            }
            // 4. 返回包含所有新密钥信息的成功结果
            return Result.success("批量生成成功", keysToInsert);
        } catch (Exception e) {
            log.error("批量生成 License Key 时数据库出错", e);
            // 可能是因为生成的 key 碰巧重复了 (概率极低)，或其他数据库问题
            return Result.fail(500, "批量生成失败，数据库错误");
        }
    }
    /**
     * [修改] 管理员为许可证续期 和/或 更新备注
     * @param keyId 要操作的许可证ID
     * @param request 包含续期天数 和/或 客户备注的请求
     * @return 操作结果
     */
    @Transactional
    public Result<LicenseKey> renewKey(Long keyId, @Valid RenewLicenseRequest request) {
        LicenseKey key = licenseKeyMapper.findById(keyId);
        if (key == null) {
            return Result.fail(404, "未找到指定的许可证");
        }

        // 只允许为已激活或已过期的许可证操作
        if (!"ACTIVATED".equals(key.getStatus()) && !"EXPIRED".equals(key.getStatus())) {
            return Result.fail(400, "只有已激活或已过期的许可证才能操作");
        }

        // 检查是否至少提供了一个操作
        if (request.getDurationInDays() == null && request.getTipCustomer() == null) {
            return Result.fail(400, "未提供任何更新信息（续期天数或备注）");
        }

        // --- 1. 处理续期（如果提供了天数） ---
        Date newExpirationDate = null;
        if (request.getDurationInDays() != null) {
            // 判定基准时间：是 "现在" 还是 "原过期时间"？取两者中更晚的那个。
            LocalDateTime baseTime = LocalDateTime.now();
            if (key.getExpirationDate() != null && key.getExpirationDate().isAfter(baseTime)) {
                baseTime = key.getExpirationDate();
            }

            // 在基准时间上增加天数
            LocalDateTime newExpirationDateTime = baseTime.plus(request.getDurationInDays(), ChronoUnit.DAYS);
            newExpirationDate = Date.from(newExpirationDateTime.atZone(ZoneId.systemDefault()).toInstant());
            log.info("管理员已为 Key ID: {} 续期 {} 天，新到期时间: {}", keyId, request.getDurationInDays(), newExpirationDateTime);
        }

        // --- 2. 处理备注（如果提供了备注） ---
        String newTipCustomer = request.getTipCustomer();
        if (newTipCustomer != null) {
            log.info("Key ID: {} 的备注已更新。", keyId);
        }

        // --- 3. 调用 Mapper 执行更新 ---
        // (如果只续期, newTipCustomer 为 null, mapper 会忽略)
        // (如果只改备注, newExpirationDate 为 null, mapper 会忽略)
        licenseKeyMapper.renew(keyId, newExpirationDate, newTipCustomer);


        // 返回更新后的完整信息给前端
        return Result.success("更新成功", licenseKeyMapper.findById(keyId));
    }


    /**
     * [已修改] 管理员分页获取所有许可证密钥
     * @param request 包含分页信息的请求体
     * @return 封装好的分页结果
     */
    public Result<PagedResult<LicenseKey>> getAllKeysPaginated(PageRequest request) {
        // [新增] 从 DTO 获取参数并设置默认值
        int pageNumber = (request.getPage() != null && request.getPage() >= 0) ? request.getPage() : 0;
        int pageSize = (request.getSize() != null && request.getSize() > 0) ? request.getSize() : 10;

        try {
            // 1. 计算 offset
            int offset = pageNumber * pageSize;

            // 2. 调用 Mapper 获取当前页数据和总数
            List<LicenseKey> keys = licenseKeyMapper.findAllPaginated(pageSize, offset);
            long totalElements = licenseKeyMapper.countAll();

            // 3. 计算总页数
            int totalPages = (int) Math.ceil((double) totalElements / pageSize);

            // 4. 组装 PagedResult 对象
            PagedResult<LicenseKey> pagedResult = new PagedResult<>(pageNumber, pageSize, totalPages, totalElements, keys);

            return Result.success("查询成功", pagedResult);
        } catch (Exception e) {
            log.error("分页查询所有 License Key 时出错", e);
            return Result.fail(500, "查询失败，服务器错误");
        }
    }

    /**
     * [新增] 管理员分页获取所有待审批的许可证密钥
     * @param pageNumber 页码 (从 0 开始)
     * @param pageSize 每页大小
     * @return 封装好的分页结果
     */
    public Result<PagedResult<LicenseKey>> getPendingKeysPaginated(int pageNumber, int pageSize) {
        try {
            // 1. 计算 offset
            int offset = pageNumber * pageSize;

            // 2. 调用 Mapper 获取当前页数据和总数
            //    (这两个方法你已经在 Mapper 中定义好了)
            List<LicenseKey> keys = licenseKeyMapper.findPendingPaginated(pageSize, offset);
            long totalElements = licenseKeyMapper.countPending();

            // 3. 计算总页数
            int totalPages = (int) Math.ceil((double) totalElements / pageSize);

            // 4. 组装 PagedResult 对象 (假设 PagedResult 构造函数与你其他方法一致)
            PagedResult<LicenseKey> pagedResult = new PagedResult<>(pageNumber, pageSize, totalPages, totalElements, keys);

            return Result.success("查询成功", pagedResult);
        } catch (Exception e) {
            log.error("分页查询待审批 License Key 时出错", e);
            return Result.fail(500, "查询失败，服务器错误");
        }
    }
    /**
     * [新增] 管理员分页获取所有已过期的许可证密钥
     * @param pageNumber 页码 (从 0 开始)
     * @param pageSize 每页大小
     * @return 封装好的分页结果
     */
    public Result<PagedResult<LicenseKey>> getExpiredKeysPaginated(int pageNumber, int pageSize) {
        return getExpiredKeysPaginatedWithCondition(pageNumber, pageSize, null);
    }

    /**
     * [新增] 管理员分页获取已过期的许可证密钥（支持 tip_customer 条件查询）
     * @param pageNumber 页码 (从 0 开始)
     * @param pageSize 每页大小
     * @param tipCustomer 客户备注条件（可为空，为空则不进行条件过滤）
     * @return 封装好的分页结果
     */
    public Result<PagedResult<LicenseKey>> getExpiredKeysPaginatedWithCondition(int pageNumber, int pageSize, String tipCustomer) {
        try {
            // 1. 计算 offset
            int offset = pageNumber * pageSize;

            // 2. 根据是否有条件调用不同的 Mapper 方法
            List<LicenseKey> keys;
            long totalElements;
            
            if (tipCustomer != null && !tipCustomer.trim().isEmpty()) {
                // 有条件查询
                keys = licenseKeyMapper.findExpiredPaginatedByTipCustomer(pageSize, offset, tipCustomer.trim());
                totalElements = licenseKeyMapper.countExpiredByTipCustomer(tipCustomer.trim());
                log.info("按条件查询已过期 License Key，条件: {}，返回 {} 条记录", tipCustomer.trim(), keys.size());
            } else {
                // 无条件查询
                keys = licenseKeyMapper.findExpiredPaginated(pageSize, offset);
                totalElements = licenseKeyMapper.countExpired();
                log.info("查询所有已过期 License Key，返回 {} 条记录", keys.size());
            }

            // 3. 计算总页数
            int totalPages = (int) Math.ceil((double) totalElements / pageSize);

            // 4. 组装 PagedResult 对象
            PagedResult<LicenseKey> pagedResult = new PagedResult<>(pageNumber, pageSize, totalPages, totalElements, keys);

            return Result.success("查询成功", pagedResult);
        } catch (Exception e) {
            log.error("分页查询已过期 License Key 时出错", e);
            return Result.fail(500, "查询失败，服务器错误");
        }
    }
    /**
     * [新增] 单独更新一个许可证的备注
     * @param keyId 要更新的许可证ID
     * @param request 包含新备注的请求
     * @return 操作结果
     */
    @Transactional
    public Result<LicenseKey> updateKeyNote(Long keyId, UpdateNoteRequest request) {
        // 1. 检查许可证是否存在
        LicenseKey key = licenseKeyMapper.findById(keyId);
        if (key == null) {
            return Result.fail(404, "未找到指定的许可证");
        }

        // 2. 获取新备注 (可以为 null)
        String newNote = request.getTipCustomer();

        // 3. 调用 Mapper 更新备注
        licenseKeyMapper.updateNote(keyId, newNote);

        log.info("Key ID: {} 的备注已更新。", keyId);

        // 4. 返回更新后的完整信息
        return Result.success("备注更新成功", licenseKeyMapper.findById(keyId));
    }

}