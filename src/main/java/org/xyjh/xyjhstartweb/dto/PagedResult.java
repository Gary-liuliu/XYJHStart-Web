package org.xyjh.xyjhstartweb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页查询结果的通用封装 DTO
 * @param <T> 分页数据的类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedResult<T> {
    // 当前页码 (从 0 开始)
    private int pageNumber;

    // 每页大小
    private int pageSize;

    // 总页数
    private int totalPages;

    // 总元素数量
    private long totalElements;

    // 当前页的数据列表
    private List<T> content;
}