package com.blog.blog_system.controller;

import com.blog.blog_system.entity.Comment;
import com.blog.blog_system.mapper.CommentMapper;
import com.blog.blog_system.service.BlogService; // 引入 Service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/comment")
@CrossOrigin
public class CommentController {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private BlogService blogService; // ✨✨ 注入 Service

    // 发表评论接口 (✨✨ 修改：改为调用 Service 以计算评分)
    @PostMapping("/add")
    public String add(@RequestBody Comment comment) {
        // commentMapper.insert(comment); // 旧代码
        return blogService.addComment(comment); // 新代码
    }

    // 获取某篇博客的评论列表
    @GetMapping("/list/{blogId}")
    public List<Comment> list(@PathVariable Long blogId) {
        return commentMapper.findByBlogId(blogId);
    }
}