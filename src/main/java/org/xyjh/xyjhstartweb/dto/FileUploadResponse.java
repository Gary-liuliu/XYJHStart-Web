package org.xyjh.xyjhstartweb.dto;

import lombok.Data;

/**
 * 文件上传响应DTO
 */
@Data
public class FileUploadResponse {
    /**
     * 文件访问URL
     */
    private String url;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 上传时间
     */
    private String uploadTime;
    
    public FileUploadResponse() {}
    
    public FileUploadResponse(String url, String fileName, Long fileSize, String fileType, String uploadTime) {
        this.url = url;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.uploadTime = uploadTime;
    }
}