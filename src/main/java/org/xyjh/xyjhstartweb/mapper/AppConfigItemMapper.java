package org.xyjh.xyjhstartweb.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.xyjh.xyjhstartweb.entity.AppConfigItem;

import java.util.List;

@Mapper
public interface AppConfigItemMapper {

    /**
     * 根据配置类型查询最近的 10 条记录
     * @param configType 配置类型：1 兑换码，2 活动代码
     * @return 返回按 ID 降序排列的最近 10 条记录
     */
    @Select("SELECT * FROM app_config_items WHERE config_type = #{configType} ORDER BY id DESC LIMIT 10")
    List<AppConfigItem> findRecentByType(@Param("configType") Integer configType);

    /**
     * 根据配置类型查询最近的 1 条记录（用于活动代码）
     * @param configType 配置类型：1 兑换码，2 活动代码
     * @return 返回按 ID 降序排列的最近 1 条记录
     */
    @Select("SELECT * FROM app_config_items WHERE config_type = #{configType} ORDER BY id DESC LIMIT 1")
    AppConfigItem findLatestByType(@Param("configType") Integer configType);

    /**
     * 检查指定类型和配置值的记录是否已存在
     * @param configType 配置类型
     * @param configValue 配置值
     * @return 返回存在的记录，如果不存在则返回 null
     */
    @Select("SELECT * FROM app_config_items WHERE config_type = #{configType} AND config_value = #{configValue} LIMIT 1")
    AppConfigItem findByTypeAndValue(@Param("configType") Integer configType, @Param("configValue") String configValue);

    /**
     * 插入一条新的配置记录
     * @param configItem 包含配置类型和配置值的实体对象
     * @return 返回影响的行数
     */
    @Insert("INSERT INTO app_config_items (config_type, config_value) VALUES (#{configType}, #{configValue})")
    int insert(AppConfigItem configItem);
}
