package com.blog.blog_system.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin
public class FileController {

    @PostMapping
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return "上传失败，文件为空";
        }

        // 1. 准备文件夹
        String projectPath = System.getProperty("user.dir");
        File uploadDir = new File(projectPath + "/uploads");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs(); // 如果没有 uploads 文件夹，自动创建
        }

        // 2. 生成唯一文件名 (防止文件名冲突)
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + suffix;

        // 3. 保存文件到硬盘
        File dest = new File(uploadDir, newFileName);
        file.transferTo(dest);

        // 4. 返回可访问的图片 URL
        return "http://localhost:8080/images/" + newFileName;
    }
}