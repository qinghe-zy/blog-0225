package com.blog.blog_system.controller;

import com.blog.blog_system.common.Result; // ✨ 引入 Result
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

    @PostMapping("/add")
    public Result<String> add(@RequestBody Comment comment) {
        return Result.success(null, blogService.addComment(comment));
    }

    @GetMapping("/list/{blogId}")
    public Result<List<Comment>> list(@PathVariable Long blogId) {
        return Result.success(commentMapper.findByBlogId(blogId));
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id, @RequestParam Long userId) {
        String msg = blogService.deleteComment(id, userId);
        if ("删除成功".equals(msg)) {
            return Result.success(null, msg);
        } else {
            return Result.error(msg);
        }
    }

    @GetMapping("/getScore")
    public Result<Double> getScore(@RequestParam Long userId, @RequestParam Long blogId) {
        Double score = commentMapper.getUserRawScore(userId, blogId);
        return Result.success(score != null ? score : 0.0);
    }
}