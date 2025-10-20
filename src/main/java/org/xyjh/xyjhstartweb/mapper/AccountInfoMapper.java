package org.xyjh.xyjhstartweb.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.xyjh.xyjhstartweb.entity.AccountInfo;

@Mapper
public interface AccountInfoMapper {

    @Insert("INSERT INTO account_info (license_id, account, password) VALUES (#{licenseId}, #{account}, #{password})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertAccount(AccountInfo accountInfo);
}