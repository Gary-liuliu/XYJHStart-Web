package org.xyjh.xyjhstartweb.mapper;


import java.util.List;
import org.apache.ibatis.annotations.*;
import org.xyjh.xyjhstartweb.entity.AdminUser;

@Mapper
public interface AdminUserMapper {

    @Select("SELECT * FROM admin_user WHERE username = #{username}")
    AdminUser findByUsername(@Param("username") String username);

    @Insert("INSERT INTO admin_user(username, password, role) VALUES(#{username}, #{password}, #{role})")
    int insert(AdminUser adminUser);

    /**
     * [新增] 根据用户名更新密码
     * @param username 要更新的用户名
     * @param newPassword 哈希后的新密码
     */
    @Update("UPDATE admin_user SET password = #{newPassword} WHERE username = #{username}")
    int updatePassword(@Param("username") String username, @Param("newPassword") String newPassword);

    /**
     * [新增] 根据 ID 删除用户
     */
    @Delete("DELETE FROM admin_user WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * [新增] 统计所有管理员用户的数量
     */
    @Select("SELECT COUNT(*) FROM admin_user")
    long count();

    /**
     * [新增] 根据 ID 查找用户
     */
    @Select("SELECT * FROM admin_user WHERE id = #{id}")
    AdminUser findById(@Param("id") Long id);
    /**
     * [新增] 查询所有管理员账户
     * @return 管理员用户列表
     */
    @Select("SELECT * FROM admin_user ORDER BY id ASC")
    List<AdminUser> findAll();

}