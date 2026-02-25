package com.blog.blog_system.controller;

import com.blog.blog_system.entity.Comment;
import com.blog.blog_system.mapper.CommentMapper;
import com.blog.blog_system.service.BlogService;
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
    private BlogService blogService;

    // 发表评论 (已升级逻辑)
    @PostMapping("/add")
    public String add(@RequestBody Comment comment) {
        return blogService.addComment(comment);
    }

    // 获取列表 (Mapper已修改，会自动过滤空内容)
    @GetMapping("/list/{blogId}")
    public List<Comment> list(@PathVariable Long blogId) {
        return commentMapper.findByBlogId(blogId);
    }

    // ✨✨✨ 新增：删除评论接口 ✨✨✨
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id, @RequestParam Long userId) {
        return blogService.deleteComment(id, userId);
    }
    /**
     * 获取当前登录用户对该文章的评分状态
     * 用于前端星星的常驻显示
     */
    @GetMapping("/getScore")
    public Double getScore(@RequestParam Long userId, @RequestParam Long blogId) {
        Double score = commentMapper.getUserRawScore(userId, blogId);
        return score != null ? score : 0.0;
    }
}