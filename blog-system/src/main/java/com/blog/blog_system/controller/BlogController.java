package com.blog.blog_system.controller;

import com.blog.blog_system.common.Result;
import com.blog.blog_system.entity.Blog;
import com.blog.blog_system.entity.User;
import com.blog.blog_system.mapper.BlogMapper;
import com.blog.blog_system.mapper.UserMapper;
import com.blog.blog_system.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.blog.blog_system.mapper.VisitLogMapper;

import java.util.List;

@RestController
@RequestMapping("/api/blog")
@CrossOrigin
public class BlogController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private VisitLogMapper visitLogMapper;

    @Autowired
    private UserMapper userMapper; // ✨ 注入用于查角色权限

    // ✨ 接收 userId 动态返回点赞/收藏状态，并过滤拉黑文章
    @GetMapping("/all")
    public Result<List<Blog>> list(@RequestParam(defaultValue = "0") Long userId) {
        return Result.success(blogService.getAllBlogs(userId));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody Blog blog) {
        return Result.success(null, blogService.saveBlog(blog));
    }

    // ✨ 接收 userId
    @GetMapping("/search")
    public Result<List<Blog>> search(@RequestParam String keyword, @RequestParam(defaultValue = "0") Long userId) {
        return Result.success(blogService.searchBlogs(keyword, userId));
    }

    // ✨✨ 核心：超级管理员删除豁免 ✨✨
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id, @RequestParam Long userId) {
        User operator = userMapper.findById(userId);
        Blog blog = blogMapper.findById(id);

        if (blog == null) return Result.error("文章不存在");

        // 权限判定: 作者本人 or 管理员(role == 1)
        boolean isAuthor = blog.getAuthor().equals(operator.getUsername()) || blog.getAuthor().equals(operator.getNickname());
        boolean isAdmin = operator.getRole() != null && operator.getRole() == 1;

        if (isAuthor || isAdmin) {
            blogService.deleteBlog(id);
            return Result.success(null, isAdmin ? "管理员强行删除成功" : "删除成功");
        }
        return Result.error("权限不足，无法删除他人文章");
    }

    @GetMapping("/detail/{id}")
    public Result<Blog> detail(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        return Result.success(blogService.getBlogDetail(id, userId));
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody Blog blog) {
        return Result.success(null, blogService.updateBlog(blog));
    }

    @PostMapping("/like")
    public Result<String> toggleLike(@RequestParam Long blogId, @RequestParam Long userId) {
        String msg = blogService.toggleLike(blogId, userId);
        return Result.success(null, msg);
    }

    @GetMapping("/checkLike")
    public Result<Boolean> checkLike(@RequestParam Long blogId, @RequestParam Long userId) {
        return Result.success(blogMapper.checkIsLiked(userId, blogId) > 0);
    }

    @GetMapping("/my-likes")
    public Result<List<Blog>> myLikes(@RequestParam Long userId) {
        return Result.success(blogMapper.findLikedBlogs(userId));
    }

    // ✨ 接收 userId
    @GetMapping("/hot")
    public Result<List<Blog>> hot(@RequestParam(defaultValue = "0") Long userId) {
        return Result.success(blogService.getHotBlogs(userId));
    }

    @GetMapping("/history")
    public Result<List<Blog>> history(@RequestParam Long userId) {
        return Result.success(blogService.getRecentBlogs(userId));
    }

    @PostMapping("/duration")
    public Result<String> logDuration(@RequestParam Long userId,
                                      @RequestParam Long blogId,
                                      @RequestParam Integer seconds) {
        if (seconds <= 0) return Result.error("无效时长");
        visitLogMapper.updateDuration(userId, blogId, seconds);
        return Result.success("记录成功");
    }

    @GetMapping("/related/{id}")
    public Result<List<Blog>> related(@PathVariable Long id) {
        return Result.success(blogService.getRelatedBlogs(id));
    }

    @GetMapping("/recommend")
    public Result<List<Blog>> recommend(@RequestParam Long userId) {
        return Result.success(blogService.getPersonalizedBlogs(userId));
    }
}