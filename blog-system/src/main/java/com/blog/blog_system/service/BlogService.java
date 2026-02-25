package com.blog.blog_system.service;

import com.blog.blog_system.entity.Blog;
import com.blog.blog_system.entity.Comment;
import com.blog.blog_system.mapper.BlogMapper;
import com.blog.blog_system.mapper.CommentMapper;
import com.blog.blog_system.mapper.VisitLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.blog.blog_system.mapper.CommentMapper;
import java.util.*;

@Service
public class BlogService {

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private VisitLogMapper visitLogMapper;

    @Autowired
    private CommentMapper commentMapper;

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
     * ✨✨✨ 升级：个性化推荐 + 热门兜底策略 ✨✨✨
     * 解决“猜你喜欢”只显示一篇文章的问题
     */
    public List<Blog> getPersonalizedBlogs(Long userId) {
        List<Blog> result = new ArrayList<>();

        // 1. 获取三种行为的标签数据
        List<String> viewedTags = visitLogMapper.selectViewedTags(userId);
        List<String> likedTags = visitLogMapper.selectLikedTags(userId);
        List<String> collectedTags = visitLogMapper.selectCollectedTags(userId);

        // 2. 如果有行为数据，计算最高分标签
        if (!viewedTags.isEmpty() || !likedTags.isEmpty() || !collectedTags.isEmpty()) {
            Map<String, Integer> tagScores = new HashMap<>();
            calculateScore(tagScores, viewedTags, 1);
            calculateScore(tagScores, likedTags, 3);
            calculateScore(tagScores, collectedTags, 5);

            // 找出 Top 1 标签
            String topTag = "";
            int maxScore = 0;
            for (Map.Entry<String, Integer> entry : tagScores.entrySet()) {
                if (entry.getValue() > maxScore) {
                    maxScore = entry.getValue();
                    topTag = entry.getKey();
                }
            }

            // 如果有 Top 标签，先查它
            if (!topTag.isEmpty()) {
                result = blogMapper.findBlogsByTag(topTag);
            }
        }

        // 3. ✨✨ 兜底策略：如果推荐结果少于 5 篇，用热门文章补齐 ✨✨
        if (result.size() < 5) {
            List<Blog> hotBlogs = blogMapper.findHotBlogs();
            for (Blog hot : hotBlogs) {
                // 去重：如果结果里还没有这篇热门文章，就加进去
                boolean exists = false;
                for (Blog existing : result) {
                    if (existing.getId().equals(hot.getId())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    result.add(hot);
                }
            }
        }

        return result;
    }

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

    @Transactional(rollbackFor = Exception.class)
    public String toggleLike(Long blogId, Long userId) {
        int count = blogMapper.checkIsLiked(userId, blogId);
        if (count == 0) {
            try {
                blogMapper.addLike(userId, blogId);
                blogMapper.incrementLikes(blogId);
                return "点赞成功";
            } catch (Exception e) {
                return "您已点赞";
            }
        } else {
            blogMapper.removeLike(userId, blogId);
            blogMapper.decrementLikes(blogId);
            return "取消成功";
        }
    }


    private void updateBlogAverageScore(Long blogId) {
        Double avgScore = commentMapper.calculateAvgScore(blogId);
        if (avgScore != null) {
            double formattedScore = (double) Math.round(avgScore * 10) / 10;
            blogMapper.updateScore(blogId, formattedScore);
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public String addComment(Comment comment) {
        // A. 如果这次操作包含评分 (score > 0)
        // 先把该用户之前在这篇文章下的所有评分重置为 0
        // 这样计算平均分时，旧分数就不会被算进去了
        if (comment.getScore() != null && comment.getScore() > 0) {
            commentMapper.clearPreviousScores(comment.getUserId(), comment.getBlogId());
        }

        // B. 插入新评论
        commentMapper.insert(comment);

        // C. 只有当这次操作包含评分时，才重新计算平均分
        // (如果只是纯文字回复，不需要重算，节省资源)
        if (comment.getScore() != null && comment.getScore() > 0) {
            updateBlogAverageScore(comment.getBlogId());
        }
        return "操作成功";
    }

    /**
     * ✨✨✨ 新增：删除评论逻辑 ✨✨✨
     */
    @Transactional(rollbackFor = Exception.class)
    public String deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.findById(commentId);
        if (comment == null) return "评论不存在";

        // 权限校验：只有发布者自己能删
        if (!comment.getUserId().equals(userId)) {
            return "无权删除";
        }

        // 1. 删除
        commentMapper.deleteById(commentId);

        // 2. 如果这条评论包含分数，删除后需要重新计算平均分
        if (comment.getScore() != null && comment.getScore() > 0) {
            updateBlogAverageScore(comment.getBlogId());
        }

        return "删除成功";
    }
}