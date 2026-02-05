package org.xyjh.xyjhstartweb.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.*;
import org.xyjh.xyjhstartweb.entity.LicenseKey;

import java.util.Date;
import java.util.List;
@Mapper
public interface LicenseKeyMapper {

    /**
     * 根据 license_key 字符串查询许可证信息
     */
    @Select("SELECT * FROM license_key WHERE license_key = #{licenseKey}")
    LicenseKey findByLicenseKey(@Param("licenseKey") String licenseKey);

    /**
     * 激活指定的 license_key，将其状态更新为 ACTIVATED 并绑定设备ID
     */
    @Update("UPDATE license_key SET status = 'ACTIVATED', device_id = #{deviceId}, activation_date = #{activationDate} WHERE license_key = #{licenseKey}")
    int activate(@Param("licenseKey") String licenseKey, @Param("deviceId") String deviceId, @Param("activationDate") Date activationDate);


    /**
     * 插入一个新的许可证密钥
     * @param licenseKey 包含 licenseKey 字符串和客户备注的实体对象
     * @return 返回影响的行数
     */
    @Insert("INSERT INTO license_key (license_key, status, tip_customer) VALUES (#{licenseKey.licenseKey}, 'AVAILABLE', #{licenseKey.tipCustomer})")
    @Options(useGeneratedKeys = true, keyProperty = "licenseKey.id") // 插入后获取自增主键ID
    int insert(@Param("licenseKey") LicenseKey licenseKey);

    /**
     * 查询所有的许可证密钥 (用于后台列表展示)
     * 在生产环境中，当密钥数量巨大时，应考虑使用分页查询。
     * @return 返回所有许可证密钥的列表
     */
    @Select("SELECT * FROM license_key ORDER BY id DESC")
    List<LicenseKey> findAll();

    /**
     * 根据 ID 查询单个许可证密钥
     */
    @Select("SELECT * FROM license_key WHERE id = #{id}")
    LicenseKey findById(@Param("id") Long id);

    /**
     * 更新许可证密钥信息 (例如：吊销、修改备注)
     * @param licenseKey 包含要更新字段的实体对象
     * @return 返回影响的行数
     */
    @Update("UPDATE license_key SET status = #{status}, tip_customer = #{tipCustomer} WHERE id = #{id}")
    int update(LicenseKey licenseKey);

    /**
     * 根据 ID 删除一个许可证密钥
     * @param id 要删除的密钥的主键ID
     * @return 返回影响的行数
     */
    @Delete("DELETE FROM license_key WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * [新] 提交激活申请：将状态更新为 PENDING_APPROVAL 并绑定设备ID
     */
    @Update("UPDATE license_key SET status = 'PENDING_APPROVAL', device_id = #{deviceId} WHERE license_key = #{licenseKey}")
    int submitForApproval(@Param("licenseKey") String licenseKey, @Param("deviceId") String deviceId);

    /**
     * [修改] 管理员批准：更新状态、激活/过期日期，并可选更新备注
     * @param id 许可证ID
     * @param activationDate 激活日期
     * @param expirationDate 过期日期
     * @param tipCustomer 客户备注 (如果为 null, 则不更新)
     * @return 影响的行数
     */
    @Update({
            "<script>",
            "UPDATE license_key",
            "  <set>",
            "    status = 'ACTIVATED',",
            "    activation_date = #{activationDate},",
            "    expiration_date = #{expirationDate},",
            "    <if test='tipCustomer != null'>tip_customer = #{tipCustomer}</if>",
            "  </set>",
            "WHERE id = #{id}",
            "</script>"
    })
    int approve(@Param("id") Long id,
                @Param("activationDate") Date activationDate,
                @Param("expirationDate") Date expirationDate,
                @Param("tipCustomer") String tipCustomer);
    /**
     * [新] 查询指定状态的所有密钥（用于后台查询待审批列表）
     */
    @Select("SELECT * FROM license_key WHERE status = #{status} ORDER BY id DESC")
    List<LicenseKey> findByStatus(@Param("status") String status);

    /**
     * 分页查询所有许可证密钥
     * @param limit 每页数量
     * @param offset 起始位置 (偏移量)
     * @return 返回当前页的许可证密钥列表
     */
    @Select("SELECT * FROM license_key ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}")
    List<LicenseKey> findAllPaginated(@Param("limit") int limit, @Param("offset") int offset);

    /**
     * [新增] 根据 tip_customer 条件分页查询所有许可证密钥
     * @param limit 每页数量
     * @param offset 起始位置
     * @param tipCustomer 客户备注条件（可为空）
     * @return 返回符合条件的许可证密钥列表
     */
    @Select({
        "SELECT * FROM license_key WHERE tip_customer LIKE CONCAT('%', #{tipCustomer}, '%') ",
        "ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}"
    })
    List<LicenseKey> findAllPaginatedByTipCustomer(
        @Param("limit") int limit, 
        @Param("offset") int offset, 
        @Param("tipCustomer") String tipCustomer);
    /**
     * 查询所有许可证密钥的总数
     * @return 总数
     */
    @Select("SELECT COUNT(*) FROM license_key")
    long countAll();

    /**
     * [新增] 根据 tip_customer 条件查询许可证密钥的总数
     * @param tipCustomer 客户备注条件（可为空）
     * @return 符合条件的总数
     */
    @Select({
        "SELECT COUNT(*) FROM license_key WHERE tip_customer LIKE CONCAT('%', #{tipCustomer}, '%')"
    })
    long countAllByTipCustomer(@Param("tipCustomer") String tipCustomer);
    /**
     * 分页查询所有已激活的许可证密钥
     * @param limit 每页数量
     * @param offset 起始位置
     * @return 返回当前页的已激活许可证密钥列表
     */
    @Select("SELECT * FROM license_key WHERE status = 'ACTIVATED' ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}")
    List<LicenseKey> findActivatedPaginated(@Param("limit") int limit, @Param("offset") int offset);
    /**
     * 查询所有已激活的许可证密钥的总数
     * @return 总数
     */
    @Select("SELECT COUNT(*) FROM license_key WHERE status = 'ACTIVATED'")
    long countActivated();
    /**
     * [新增] 批量插入许可证密钥
     * @param keys 要插入的许可证密钥列表
     * @return 返回影响的行数
     */
    @Insert({
            "<script>",
            "INSERT INTO license_key (license_key, status) VALUES",
            "<foreach collection='keys' item='key' separator=','>",
            "(#{key.licenseKey}, 'AVAILABLE')",
            "</foreach>",
            "</script>"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id") // Note: This might only return the last generated ID for some DBs
    int batchInsert(@Param("keys") List<LicenseKey> keys);
    /**
     * [新增] 查找所有已过期但状态仍为 ACTIVATED 的许可证
     * @return 过期的许可证列表
     */
    @Select("SELECT * FROM license_key WHERE status = 'ACTIVATED' AND expiration_date < NOW()")
    List<LicenseKey> findExpiredButActiveKeys();
    /**
     * [新增] 批量更新许可证的状态
     * @param ids 要更新的许可证ID列表
     * @param status 新的状态
     * @return 影响的行数
     */
    @Update({
            "<script>",
            "UPDATE license_key SET status = #{status} WHERE id IN",
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") String status);

    /**
     * [新增] 根据ID更新单个许可证的状态
     * @param id 许可证ID
     * @param status 新的状态
     * @return 影响的行数
     */
    @Update("UPDATE license_key SET status = #{status} WHERE id = #{id}")
    int updateStatusById(@Param("id") Long id, @Param("status") String status);

    /**
     * [修改] 续期或更新一个许可证：
     * 动态更新过期日期、备注，并将状态设为 ACTIVATED
     * @param id 要续期的许可证ID
     * @param newExpirationDate 新的过期日期 (如果为 null, 则不更新)
     * @param tipCustomer 新的客户备注 (如果为 null, 则不更新)
     * @return 影响的行数
     */
    @Update({
            "<script>",
            "UPDATE license_key",
            "  <set>",
            "    status = 'ACTIVATED',", // 续期/更新操作总是将其设置为激活状态
            "    <if test='newExpirationDate != null'>expiration_date = #{newExpirationDate},</if>",
            "    <if test='tipCustomer != null'>tip_customer = #{tipCustomer},</if>",
            "  </set>",
            "WHERE id = #{id}",
            "</script>"
    })
    int renew(@Param("id") Long id,
              @Param("newExpirationDate") Date newExpirationDate,
              @Param("tipCustomer") String tipCustomer);
    /**
     * [新增] 分页查询所有待审批的许可证密钥
     * @param limit 每页数量
     * @param offset 起始位置
     * @return 返回当前页的待审批许可证密钥列表
     */
    @Select("SELECT * FROM license_key WHERE status = 'PENDING_APPROVAL' ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}")
    List<LicenseKey> findPendingPaginated(@Param("limit") int limit, @Param("offset") int offset);

    /**
     * [新增] 查询所有待审批的许可证密钥的总数
     * @return 总数
     */
    @Select("SELECT COUNT(*) FROM license_key WHERE status = 'PENDING_APPROVAL'")
    long countPending();

    /**
     * [新增] 分页查询所有已过期的许可证密钥
     * @param limit 每页数量
     * @param offset 起始位置
     * @return 返回当前页的已过期许可证密钥列表
     */
    @Select("SELECT * FROM license_key WHERE status = 'EXPIRED' ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}")
    List<LicenseKey> findExpiredPaginated(@Param("limit") int limit, @Param("offset") int offset);

    /**
     * [新增] 根据 tip_customer 条件分页查询已过期的许可证密钥
     * @param limit 每页数量
     * @param offset 起始位置
     * @param tipCustomer 客户备注条件（可为空）
     * @return 返回符合条件的已过期许可证密钥列表
     */
    @Select({
        "SELECT * FROM license_key WHERE status = 'EXPIRED' AND tip_customer LIKE CONCAT('%', #{tipCustomer}, '%') ",
        "ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}"
    })
    List<LicenseKey> findExpiredPaginatedByTipCustomer(
        @Param("limit") int limit, 
        @Param("offset") int offset, 
        @Param("tipCustomer") String tipCustomer);

    /**
     * [新增] 查询所有已过期的许可证密钥的总数
     * @return 总数
     */
    @Select("SELECT COUNT(*) FROM license_key WHERE status = 'EXPIRED'")
    long countExpired();

    /**
     * [新增] 根据 tip_customer 条件查询已过期许可证密钥的总数
     * @param tipCustomer 客户备注条件（可为空）
     * @return 符合条件的总数
     */
    @Select({
        "SELECT COUNT(*) FROM license_key WHERE status = 'EXPIRED' AND tip_customer LIKE CONCAT('%', #{tipCustomer}, '%')"
    })
    long countExpiredByTipCustomer(@Param("tipCustomer") String tipCustomer);
    /**
     * [新增] 单独更新指定ID的许可证备注
     * @param id 许可证ID
     * @param tipCustomer 新的备注内容 (可以为 null)
     * @return 影响的行数
     */
    @Update("UPDATE license_key SET tip_customer = #{tipCustomer} WHERE id = #{id}")
    int updateNote(@Param("id") Long id, @Param("tipCustomer") String tipCustomer);
}
