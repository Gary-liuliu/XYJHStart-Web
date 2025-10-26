package org.xyjh.xyjhstartweb.service;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xyjh.xyjhstartweb.dto.ActivationRequest;
import org.xyjh.xyjhstartweb.dto.ApproveLicenseRequest;
import org.xyjh.xyjhstartweb.dto.CreateLicenseKeyRequest;
import org.xyjh.xyjhstartweb.dto.PagedResult;
import org.xyjh.xyjhstartweb.entity.LicenseKey;
import org.xyjh.xyjhstartweb.mapper.LicenseKeyMapper;
import org.xyjh.xyjhstartweb.util.Result;
import org.xyjh.xyjhstartweb.util.jwt.JwtUtil;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class LicenseService {

    @Autowired
    private LicenseKeyMapper licenseKeyMapper;

    @Autowired
    private JwtUtil jwtUtil;
    private static final Logger log = LoggerFactory.getLogger(AccountInfoService.class);


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
     * [新] 客户端轮询检查激活状态
     * @param licenseKey 客户端用于激活的 key
     * @param deviceId 客户端的设备ID
     * @return 如果已批准，返回JWT；否则返回当前状态
     */
    public Result<String> checkActivationStatus(String licenseKey, String deviceId) {
        LicenseKey key = licenseKeyMapper.findByLicenseKey(licenseKey);

        if (key == null || !deviceId.equals(key.getDeviceId())) {
            return Result.fail(401, "无效的请求");
        }

        if ("ACTIVATED".equals(key.getStatus())) {
            // 已激活！生成并返回JWT
            if (key.getExpirationDate() == null) {
                log.error("数据错误：Key {} 已激活但没有过期时间！", licenseKey);
                return Result.fail(500, "许可证数据异常，请联系管理员");
            }
            Date expiration = Date.from(key.getExpirationDate().atZone(java.time.ZoneId.systemDefault()).toInstant());
            String token = jwtUtil.generateToken(deviceId, licenseKey, expiration);
            return Result.success("激活成功", token);
        } else {
            // 尚未激活，告知客户端当前状态
            return Result.fail(202, "审核中，请稍后重试"); // 202 Accepted
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
    // ... (validateToken 方法需要微调以处理过期) ...
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
     * [新] 管理员批准激活申请
     * @param keyId 要批准的密钥ID
     * @param request 包含授权天数
     * @return 操作结果
     */
    public Result<LicenseKey> approveKey(Long keyId, ApproveLicenseRequest request) {
        LicenseKey key = licenseKeyMapper.findById(keyId);
        if (key == null || !"PENDING_APPROVAL".equals(key.getStatus())) {
            return Result.fail(404, "未找到待审批的许可证");
        }

        // 计算过期时间
        Instant now = Instant.now();
        Instant expirationInstant = now.plus(request.getDurationInDays(), ChronoUnit.DAYS);
        Date activationDate = Date.from(now);
        Date expirationDate = Date.from(expirationInstant);

        // 更新数据库
        licenseKeyMapper.approve(keyId, activationDate, expirationDate);
        log.info("管理员已批准 Key ID: {}，授权 {} 天", keyId, request.getDurationInDays());

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

}