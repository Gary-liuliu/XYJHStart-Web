//package org.xyjh.xyjhstartweb.service;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.io.FilenameUtils;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import org.xyjh.xyjhstartweb.dto.FileUploadResponse;
//import org.xyjh.xyjhstartweb.util.Result;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Arrays;
//import java.util.List;
//import java.util.UUID;
//
///**
// * 文件上传服务类
// */
//@Slf4j
//@Service
//public class FileUploadService {
//
//    // 允许的图片格式
//    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
//            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"
//    );
//
//    // 允许的视频格式
//    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
//            "video/mp4", "video/avi", "video/mov", "video/wmv", "video/flv",
//            "video/mkv", "video/webm", "video/3gp"
//    );
//
//    // 最大文件大小 15MB
//    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024;
//
//    @Value("${file.upload.path:D:/XYJHStart-Web/uploads}")
//    private String uploadPath;
//
//    @Value("${file.access.url:http://localhost:9191/files}")
//    private String fileAccessUrl;
//
//    /**
//     * 上传单个文件
//     */
//    public Result<FileUploadResponse> uploadFile(MultipartFile file) {
//        try {
//            // 参数校验
//            if (file == null || file.isEmpty()) {
//                return Result.fail("文件不能为空");
//            }
//
//            // 文件大小检查
//            if (file.getSize() > MAX_FILE_SIZE) {
//                return Result.fail("文件大小不能超过15MB");
//            }
//
//            // 文件类型检查
//            String contentType = file.getContentType();
//            if (!isValidFileType(contentType)) {
//                return Result.fail("不支持的文件类型，仅支持图片和视频文件");
//            }
//
//            // 原始文件名
//            String originalFilename = file.getOriginalFilename();
//            if (originalFilename == null || originalFilename.isEmpty()) {
//                return Result.fail("文件名不能为空");
//            }
//
//            // 获取文件扩展名
//            String extension = FilenameUtils.getExtension(originalFilename);
//            if (extension == null || extension.isEmpty()) {
//                return Result.error("无法识别文件类型");
//            }
//
//            // 生成唯一文件名
//            String uniqueFileName = generateUniqueFileName(extension);
//
//            // 按日期创建文件夹
//            String dateFolder = getCurrentDateFolder();
//            Path dateFolderPath = Paths.get(uploadPath, dateFolder);
//
//            // 创建目录
//            if (!Files.exists(dateFolderPath)) {
//                Files.createDirectories(dateFolderPath);
//            }
//
//            // 完整文件路径
//            Path filePath = dateFolderPath.resolve(uniqueFileName);
//
//            // 保存文件
//            file.transferTo(filePath.toFile());
//
//            // 构造访问URL
//            String fileUrl = fileAccessUrl + "/" + dateFolder + "/" + uniqueFileName;
//
//            // 创建响应对象
//            FileUploadResponse response = new FileUploadResponse(
//                    fileUrl,
//                    originalFilename,
//                    file.getSize(),
//                    contentType,
//                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
//            );
//
//            log.info("文件上传成功: {} -> {}", originalFilename, filePath.toString());
//            return Result.success(response);
//
//        } catch (IOException e) {
//            log.error("文件上传失败", e);
//            return Result.error("文件上传失败: " + e.getMessage());
//        } catch (Exception e) {
//            log.error("文件上传异常", e);
//            return Result.fail("文件上传异常: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 批量上传文件
//     */
//    public Result<List<FileUploadResponse>> uploadMultipleFiles(MultipartFile[] files) {
//        if (files == null || files.length == 0) {
//            return Result.fail("请选择要上传的文件");
//        }
//
//        if (files.length > 10) {
//            return Result.fail("一次最多只能上传10个文件");
//        }
//
//        List<FileUploadResponse> responses = new java.util.ArrayList<>();
//        StringBuilder errorMsg = new StringBuilder();
//
//        for (MultipartFile file : files) {
//            Result<FileUploadResponse> result = uploadFile(file);
//            if (result.isSuccess()) {
//                responses.add(result.getData());
//            } else {
//                errorMsg.append(result.getMessage()).append("; ");
//            }
//        }
//
//        if (responses.isEmpty()) {
//            return Result.fail("所有文件上传失败: " + errorMsg.toString());
//        }
//
//        if (errorMsg.length() > 0) {
//            return Result.success(responses, "部分文件上传成功: " + failMsg.toString());
//        }
//
//        return Result.success(responses);
//    }
//
//    /**
//     * 删除文件
//     */
//    public Result<Void> deleteFile(String filePath) {
//        try {
//            if (filePath == null || filePath.isEmpty()) {
//                return Result.fail("文件路径不能为空");
//            }
//
//            // 解析实际文件路径
//            String relativePath = filePath.replace(fileAccessUrl + "/", "");
//            Path actualFilePath = Paths.get(uploadPath, relativePath);
//
//            File file = actualFilePath.toFile();
//            if (!file.exists()) {
//                return Result.fail("文件不存在");
//            }
//
//            if (file.delete()) {
//                log.info("文件删除成功: {}", filePath);
//                return Result.success(null, "文件删除成功");
//            } else {
//                return Result.fail("文件删除失败");
//            }
//        } catch (Exception e) {
//            log.error("文件删除异常", e);
//            return Result.fail("文件删除异常: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 检查文件类型是否有效
//     */
//    private boolean isValidFileType(String contentType) {
//        return ALLOWED_IMAGE_TYPES.contains(contentType) || ALLOWED_VIDEO_TYPES.contains(contentType);
//    }
//
//    /**
//     * 生成唯一文件名
//     */
//    private String generateUniqueFileName(String extension) {
//        return UUID.randomUUID().toString().replace("-", "") + "." + extension;
//    }
//
//    /**
//     * 获取当前日期文件夹名称 (格式: yyyy/MM/dd)
//     */
//    private String getCurrentDateFolder() {
//        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
//    }
//
//    /**
//     * 初始化上传目录
//     */
//    public void initUploadDirectory() {
//        try {
//            Path path = Paths.get(uploadPath);
//            if (!Files.exists(path)) {
//                Files.createDirectories(path);
//                log.info("创建上传目录: {}", uploadPath);
//            }
//        } catch (IOException e) {
//            log.error("创建上传目录失败", e);
//        }
//    }
//}