package org.xyjh.xyjhstartweb.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 文件删除请求DTO
 */
@Data
public class FileDeleteRequest {
    /**
     * 文件路径
     */
    @NotBlank(message = "文件路径不能为空")
    private String filePath;
}