package com.blog.blog_system.service;

import com.blog.blog_system.entity.Blog;
import com.blog.blog_system.mapper.BlogMapper;
import com.blog.blog_system.mapper.VisitLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Collections;

@Service
public class BlogService {

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private VisitLogMapper visitLogMapper;

    public List<Blog> getAllBlogs() {
        return blogMapper.findAll();
    }

    public String saveBlog(Blog blog) {
        if (blog.getSummary() == null || blog.getSummary().isEmpty()) {
            String content = blog.getContent();
            String autoSummary = content.length() > 50 ? content.substring(0, 50) + "..." : content;
            blog.setSummary(autoSummary);
        }
        blogMapper.insert(blog);
        return "发布成功！";
    }

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

    public Blog getBlogDetail(Long id, Long userId) {
        blogMapper.incrementViews(id);
        if (userId != null) {
            try {
                visitLogMapper.insert(userId, id);
            } catch (Exception e) {
                System.err.println("记录浏览历史失败: " + e.getMessage());
            }
        }
        return blogMapper.findById(id);
    }

    public List<Blog> getHotBlogs() {
        return blogMapper.findHotBlogs();
    }

    public List<Blog> getRecentBlogs(Long userId) {
        return blogMapper.findRecentBlogs(userId);
    }

    /**
     * ✨✨✨ 新增：获取相关推荐逻辑 ✨✨✨
     * 策略：
     * 1. 提取当前文章的第一个标签作为核心关键词。
     * 2. 去数据库搜同标签的文章。
     * 3. 如果没标签或没搜到，降级为推荐热门文章（兜底策略）。
     */
    public List<Blog> getRelatedBlogs(Long id) {
        Blog currentBlog = blogMapper.findById(id);

        // 防御性编程：如果文章不存在或没有标签，直接返回热门
        if (currentBlog == null || currentBlog.getTags() == null || currentBlog.getTags().trim().isEmpty()) {
            return blogMapper.findHotBlogs();
        }

        // 提取第一个标签 (支持中文或英文逗号)
        String[] tags = currentBlog.getTags().split("[,，]");
        String firstTag = tags[0].trim();

        // 查询相关
        List<Blog> related = blogMapper.findRelatedBlogs(id, firstTag);

        // 防御性编程：如果也没搜到相关文章，还是返回热门，保证前端不空
        if (related == null || related.isEmpty()) {
            return blogMapper.findHotBlogs();
        }

        return related;
    }
}