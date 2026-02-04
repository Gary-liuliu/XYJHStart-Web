package org.xyjh.xyjhstartweb.dto;
import lombok.Data;

/**
 * 通用分页请求 DTO
 */
@Data
public class PageRequest {
    /**
     * 请求的页码 (从 0 开始)
     */
    private Integer page;

    /**
     * 每页的数量
     */
    private Integer size;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}