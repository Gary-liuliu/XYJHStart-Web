//package org.xyjh.xyjhstartweb.controller;
//
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.xyjh.xyjhstartweb.dto.FileDeleteRequest;
//import org.xyjh.xyjhstartweb.dto.FileUploadResponse;
//import org.xyjh.xyjhstartweb.service.FileUploadService;
//import org.xyjh.xyjhstartweb.util.Result;
//
//import java.util.List;
//
///**
// * 文件上传控制器
// */
//@Slf4j
//@RestController
//@RequestMapping("/api/files")
//@RequiredArgsConstructor
//public class FileUploadController {
//
//    private final FileUploadService fileUploadService;
//
//    /**
//     * 上传单个文件
//     */
//    @PostMapping("/upload")
//    public ResponseEntity<Result<FileUploadResponse>> uploadFile(
//            @RequestParam("file") MultipartFile file) {
//
//        log.info("收到文件上传请求，文件名: {}", file.getOriginalFilename());
//        Result<FileUploadResponse> result = fileUploadService.uploadFile(file);
//        return ResponseEntity.ok(result);
//    }
//
//    /**
//     * 批量上传文件
//     */
//    @PostMapping("/upload/batch")
//    public ResponseEntity<Result<List<FileUploadResponse>>> uploadMultipleFiles(
//            @RequestParam("files") MultipartFile[] files) {
//
//        log.info("收到批量文件上传请求，文件数量: {}", files.length);
//        Result<List<FileUploadResponse>> result = fileUploadService.uploadMultipleFiles(files);
//        return ResponseEntity.ok(result);
//    }
//
//    /**
//     * 删除文件
//     */
//    @DeleteMapping("/delete")
//    public ResponseEntity<Result<Void>> deleteFile(
//            @Valid @RequestBody FileDeleteRequest request) {
//
//        log.info("收到文件删除请求，文件路径: {}", request.getFilePath());
//        Result<Void> result = fileUploadService.deleteFile(request.getFilePath());
//        return ResponseEntity.ok(result);
//    }
//
//    /**
//     * 获取文件上传配置信息
//     */
//    @GetMapping("/config")
//    public ResponseEntity<Result<Object>> getFileUploadConfig() {
//        return ResponseEntity.ok(Result.success(new Object() {
//            public final String maxSize = "15MB";
//            public final String[] allowedImageTypes = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
//            public final String[] allowedVideoTypes = {"mp4", "avi", "mov", "wmv", "flv", "mkv", "webm", "3gp"};
//            public final int maxBatchCount = 10;
//        }));
//    }
//}