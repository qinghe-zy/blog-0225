package com.blog.blog_system.controller;

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

    // 1. 获取所有博客
    @GetMapping("/all")
    public List<Blog> list() {
        return blogService.getAllBlogs();
    }

    // 2. 发布博客
    @PostMapping("/add")
    public String add(@RequestBody Blog blog) {
        return blogService.saveBlog(blog);
    }

    // 3. 搜索接口
    @GetMapping("/search")
    public List<Blog> search(@RequestParam String keyword) {
        return blogService.searchBlogs(keyword);
    }

    // 4. 删除接口
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return "删除成功！";
    }

    // 5. ✨ 详情接口：增加 userId 参数，用于埋点记录浏览历史
    @GetMapping("/detail/{id}")
    public Blog detail(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        // 调用升级后的 Service 方法 (需确保 Service 层已处理 userId 的埋点逻辑)
        return blogService.getBlogDetail(id, userId);
    }

    // 6. 修改接口
    @PutMapping("/update")
    public String update(@RequestBody Blog blog) {
        return blogService.updateBlog(blog);
    }

    // --- 点赞相关功能 ---

    // 7. 点赞/取消点赞 (✨✨ 修改：改为调用 Service 事务方法)
    @PostMapping("/like")
    public String toggleLike(@RequestParam Long blogId, @RequestParam Long userId) {
        // 原有 Controller 逻辑存在并发风险，现已移至 Service 层
        return blogService.toggleLike(blogId, userId);
    }

    // 8. 检查是否点赞
    @GetMapping("/checkLike")
    public Boolean checkLike(@RequestParam Long blogId, @RequestParam Long userId) {
        return blogMapper.checkIsLiked(userId, blogId) > 0;
    }

    // 9. 我的点赞列表
    @GetMapping("/my-likes")
    public List<Blog> myLikes(@RequestParam Long userId) {
        return blogMapper.findLikedBlogs(userId);
    }

    // --- ✨ 新增：行为埋点与榜单功能 ---

    // 10.获取全站热门榜单
    @GetMapping("/hot")
    public List<Blog> hot() {
        return blogService.getHotBlogs();
    }

    // 11. 获取我的浏览历史
    @GetMapping("/history")
    public List<Blog> history(@RequestParam Long userId) {
        return blogService.getRecentBlogs(userId);
    }

    // 新增：上报阅读时长接口
    @PostMapping("/duration")
    public String logDuration(@RequestParam Long userId,
                              @RequestParam Long blogId,
                              @RequestParam Integer seconds) {
        if (seconds <= 0) return "无效时长";
        visitLogMapper.updateDuration(userId, blogId, seconds);
        return "记录成功";
    }

    // 获取相关推荐接口
    @GetMapping("/related/{id}")
    public List<Blog> related(@PathVariable Long id) {
        return blogService.getRelatedBlogs(id);
    }

    /**
     * 新增：首页个性化推荐接口 (猜你喜欢)
     * GET /api/blog/recommend?userId=1
     */
    @GetMapping("/recommend")
    public List<Blog> recommend(@RequestParam Long userId) {
        return blogService.getPersonalizedBlogs(userId);
    }
}