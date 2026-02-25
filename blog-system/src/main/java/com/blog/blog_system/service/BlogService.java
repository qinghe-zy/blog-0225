package com.blog.blog_system.service;

import com.blog.blog_system.entity.Blog;
import com.blog.blog_system.mapper.BlogMapper;
import com.blog.blog_system.mapper.VisitLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class BlogService {

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private VisitLogMapper visitLogMapper;

    // ... (保留 getAllBlogs, saveBlog, searchBlogs, updateBlog, deleteBlog 等原有方法，不要删除) ...

    public List<Blog> getAllBlogs() { return blogMapper.findAll(); }

    public String saveBlog(Blog blog) {
        if (blog.getSummary() == null || blog.getSummary().isEmpty()) {
            String content = blog.getContent();
            String autoSummary = content.length() > 50 ? content.substring(0, 50) + "..." : content;
            blog.setSummary(autoSummary);
        }
        blogMapper.insert(blog);
        return "发布成功！";
    }

    public List<Blog> searchBlogs(String keyword) { return blogMapper.search(keyword); }

    public String updateBlog(Blog blog) { blogMapper.update(blog); return "修改成功！"; }

    public void deleteBlog(Long id) { blogMapper.deleteById(id); }

    // 详情逻辑
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

    public List<Blog> getHotBlogs() { return blogMapper.findHotBlogs(); }

    public List<Blog> getRecentBlogs(Long userId) { return blogMapper.findRecentBlogs(userId); }

    public List<Blog> getRelatedBlogs(Long id) {
        Blog currentBlog = blogMapper.findById(id);
        if (currentBlog == null || currentBlog.getTags() == null || currentBlog.getTags().trim().isEmpty()) {
            return blogMapper.findHotBlogs();
        }
        String[] tags = currentBlog.getTags().split("[,，]");
        String firstTag = tags[0].trim();
        List<Blog> related = blogMapper.findRelatedBlogs(id, firstTag);
        if (related == null || related.isEmpty()) {
            return blogMapper.findHotBlogs();
        }
        return related;
    }

    /**
     * ✨✨✨ 升级版：首页个性化推荐 (基于加权算法) ✨✨✨
     * 权重规则：
     * - 阅读 (View): 1分
     * - 点赞 (Like): 3分
     * - 收藏 (Collect): 5分
     */
    public List<Blog> getPersonalizedBlogs(Long userId) {
        // 1. 获取三种行为的标签数据
        List<String> viewedTags = visitLogMapper.selectViewedTags(userId);
        List<String> likedTags = visitLogMapper.selectLikedTags(userId);
        List<String> collectedTags = visitLogMapper.selectCollectedTags(userId);

        // 冷启动防御：如果没有任何行为，返回热门
        if (viewedTags.isEmpty() && likedTags.isEmpty() && collectedTags.isEmpty()) {
            return blogMapper.findHotBlogs();
        }

        // 2. 定义分数统计池
        Map<String, Integer> tagScores = new HashMap<>();

        // 3. 计算阅读分数 (权重 1)
        calculateScore(tagScores, viewedTags, 1);

        // 4. 计算点赞分数 (权重 3)
        calculateScore(tagScores, likedTags, 3);

        // 5. 计算收藏分数 (权重 5)
        calculateScore(tagScores, collectedTags, 5);

        // 6. 找出最高分的标签 (Top 1)
        String topTag = "";
        int maxScore = 0;
        for (Map.Entry<String, Integer> entry : tagScores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                topTag = entry.getKey();
            }
        }

        // 7. 根据Top标签查询文章
        if (!topTag.isEmpty()) {
            return blogMapper.findBlogsByTag(topTag);
        }

        return blogMapper.findHotBlogs();
    }

    /**
     * 辅助方法：计算标签得分
     */
    private void calculateScore(Map<String, Integer> scores, List<String> tags, int weight) {
        if (tags == null) return;
        for (String tagStr : tags) {
            String[] splitTags = tagStr.split("[,，]");
            for (String t : splitTags) {
                t = t.trim();
                if (!t.isEmpty()) {
                    scores.put(t, scores.getOrDefault(t, 0) + weight);
                }
            }
        }
    }
}