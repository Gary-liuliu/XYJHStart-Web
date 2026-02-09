package org.xyjh.xyjhstartweb.mapper;

import org.apache.ibatis.annotations.*;
import org.xyjh.xyjhstartweb.dto.QueryAccountXYJHRequest;
import org.xyjh.xyjhstartweb.entity.AccountXYJH;

import java.util.List;

@Mapper
public interface AccountXYJHMapper {

    /**
     * 插入新账号
     */
    @Insert("INSERT INTO account_xyjh (account, password, account_name, buy_time, update_time, " +
            "status, buy_price, green_ticket, yellow_ticket, remark, strong_character) " +
            "VALUES (#{account}, #{password}, #{accountName}, #{buyTime}, #{updateTime}, " +
            "#{status}, #{buyPrice}, #{greenTicket}, #{yellowTicket}, #{remark}, #{strongCharacter})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertAccount(AccountXYJH account);

    /**
     * 根据ID更新账号信息
     */
    int updateAccountById(AccountXYJH account);

    /**
     * 根据ID删除账号
     */
    @Delete("DELETE FROM account_xyjh WHERE id = #{id}")
    int deleteAccountById(Long id);

    /**
     * 根据ID查询账号
     */
    @Select("SELECT * FROM account_xyjh WHERE id = #{id}")
    AccountXYJH selectAccountById(Long id);

    /**
     * 分页查询账号列表
     */
    List<AccountXYJH> selectAccountsByCondition(QueryAccountXYJHRequest request);

    /**
     * 查询账号总数（用于分页）
     */
    int countAccountsByCondition(QueryAccountXYJHRequest request);

    /**
     * 根据状态查询账号列表
     */
    @Select("SELECT * FROM account_xyjh WHERE status = #{status} ORDER BY buy_time DESC")
    List<AccountXYJH> selectAccountsByStatus(Integer status);

    /**
     * 根据账号或账号名模糊查询
     */
    @Select("SELECT * FROM account_xyjh WHERE account LIKE CONCAT('%', #{keyword}, '%') " +
            "OR account_name LIKE CONCAT('%', #{keyword}, '%') ORDER BY buy_time DESC")
    List<AccountXYJH> selectAccountsByKeyword(String keyword);
}