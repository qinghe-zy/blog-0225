package com.blog.blog_system.controller;

import com.blog.blog_system.entity.Comment;
import com.blog.blog_system.mapper.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/comment")
@CrossOrigin
public class CommentController {

    @Autowired
    private CommentMapper commentMapper;

    // 发表评论接口
    @PostMapping("/add")
    public String add(@RequestBody Comment comment) {
        commentMapper.insert(comment);
        return "评论成功";
    }

    // 获取某篇博客的评论列表
    @GetMapping("/list/{blogId}")
    public List<Comment> list(@PathVariable Long blogId) {
        return commentMapper.findByBlogId(blogId);
    }
}