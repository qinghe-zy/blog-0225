package com.blog.blog_system.controller;

import com.blog.blog_system.common.Result; // ✨ 引入 Result
import com.blog.blog_system.entity.Blog;
import com.blog.blog_system.mapper.BlogMapper;
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

    @GetMapping("/all")
    public Result<List<Blog>> list() {
        return Result.success(blogService.getAllBlogs());
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody Blog blog) {
        return Result.success(null, blogService.saveBlog(blog));
    }

    @GetMapping("/search")
    public Result<List<Blog>> search(@RequestParam String keyword) {
        return Result.success(blogService.searchBlogs(keyword));
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return Result.success(null, "删除成功！");
    }

    @GetMapping("/detail/{id}")
    public Result<Blog> detail(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        return Result.success(blogService.getBlogDetail(id, userId));
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody Blog blog) {
        return Result.success(null, blogService.updateBlog(blog));
    }

    // 点赞接口 (注意：这里现在统一返回 Result)
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

    @GetMapping("/hot")
    public Result<List<Blog>> hot() {
        return Result.success(blogService.getHotBlogs());
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