package com.blog.blog_system.controller;

import com.blog.blog_system.common.Result; // ✨ 引入 Result
import com.blog.blog_system.entity.Blog;
import com.blog.blog_system.mapper.BlogMapper;
import com.blog.blog_system.mapper.UserActionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/action")
@CrossOrigin
public class ActionController {

    @Autowired
    private UserActionMapper userActionMapper;

    @Autowired
    private BlogMapper blogMapper;

    @PostMapping("/toggle")
    public Result<String> toggle(@RequestParam Long userId,
                                 @RequestParam Long blogId,
                                 @RequestParam Integer type) {
        int count = userActionMapper.checkExist(userId, blogId, type);
        if (count > 0) {
            userActionMapper.remove(userId, blogId, type);
            if (type == 1) blogMapper.decrementCollects(blogId);
            return Result.success("已取消");
        } else {
            userActionMapper.add(userId, blogId, type);
            if (type == 1) blogMapper.incrementCollects(blogId);
            return Result.success("操作成功");
        }
    }

    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam Long userId,
                                 @RequestParam Long blogId,
                                 @RequestParam Integer type) {
        return Result.success(userActionMapper.checkExist(userId, blogId, type) > 0);
    }

    @GetMapping("/list")
    public Result<List<Blog>> list(@RequestParam Long userId,
                                   @RequestParam Integer type) {
        return Result.success(userActionMapper.selectBlogsByType(userId, type));
    }
}