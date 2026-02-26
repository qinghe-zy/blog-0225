package com.blog.blog_system.controller;

import com.blog.blog_system.common.Result; // ✨ 引入 Result
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
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }
        String projectPath = System.getProperty("user.dir");
        File uploadDir = new File(projectPath + "/uploads");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + suffix;
        File dest = new File(uploadDir, newFileName);
        file.transferTo(dest);

        return Result.success("http://localhost:8080/images/" + newFileName);
    }
}