package com.blog.blog_system.controller;

import com.blog.blog_system.common.Result;
import com.blog.blog_system.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/analyze")
    public Result<Map<String, String>> analyze(@RequestBody Map<String, String> params) {
        String content = params.get("content");
        Map<String, String> result = aiService.generateSummaryAndTags(content);
        return Result.success(result);
    }
}