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
     * [新] 管理员批准：更新状态、激活日期和【过期日期】
     */
    @Update("UPDATE license_key SET status = 'ACTIVATED', activation_date = #{activationDate}, expiration_date = #{expirationDate} WHERE id = #{id}")
    int approve(@Param("id") Long id, @Param("activationDate") Date activationDate, @Param("expirationDate") Date expirationDate);

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
     * 查询所有许可证密钥的总数
     * @return 总数
     */
    @Select("SELECT COUNT(*) FROM license_key")
    long countAll();

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


}