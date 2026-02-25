package com.blog.blog_system.service;

import com.blog.blog_system.entity.Blog;
import com.blog.blog_system.mapper.BlogMapper;
import com.blog.blog_system.mapper.VisitLogMapper; // 引入新 Mapper
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BlogService {

    @Autowired
    private BlogMapper blogMapper;

    // ✨ 注入埋点工具，用于记录浏览历史
    @Autowired
    private VisitLogMapper visitLogMapper;

    public List<Blog> getAllBlogs() {
        return blogMapper.findAll();
    }

    // 升级版的发布逻辑
    public String saveBlog(Blog blog) {
        // 模拟 AI：如果用户没填摘要，自动截取正文前 50 字
        if (blog.getSummary() == null || blog.getSummary().isEmpty()) {
            String content = blog.getContent();
            // 简单的截取逻辑，防止正文太短报错
            String autoSummary = content.length() > 50 ? content.substring(0, 50) + "..." : content;
            blog.setSummary(autoSummary);
        }

        blogMapper.insert(blog);
        return "发布成功！";
    }

    // 搜索博客的业务逻辑
    public List<Blog> searchBlogs(String keyword) {
        return blogMapper.search(keyword);
    }

    public String updateBlog(Blog blog) {
        blogMapper.update(blog);
        return "修改成功！";
    }

    public void deleteBlog(Long id) {
        blogMapper.deleteById(id);
    }

    /**
     * ✨ 升级版详情逻辑：看一次+1，并且记录是谁看的
     * @param id 博客ID
     * @param userId 用户ID (可能为null，代表游客)
     */
    public Blog getBlogDetail(Long id, Long userId) {
        blogMapper.incrementViews(id); // 浏览量+1 (旧逻辑)

        // ✨ 新增：如果是登录用户 (userId不为空)，记录浏览足迹
        if (userId != null) {
            try {
                visitLogMapper.insert(userId, id);
            } catch (Exception e) {
                // 捕获异常防止日志系统挂了影响用户看文章
                System.err.println("记录浏览历史失败: " + e.getMessage());
            }
        }

        return blogMapper.findById(id); // 再查出来
    }

    // 新接口：获取热门榜单
    public List<Blog> getHotBlogs() {
        return blogMapper.findHotBlogs();
    }

    // 新接口：获取某人的浏览历史
    public List<Blog> getRecentBlogs(Long userId) {
        return blogMapper.findRecentBlogs(userId);
    }
}