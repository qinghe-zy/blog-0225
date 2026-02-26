package com.blog.blog_system.service;

import com.blog.blog_system.entity.Blog;
import com.blog.blog_system.entity.Comment;
import com.blog.blog_system.entity.User; // ✨ 补充导入 User 实体
import com.blog.blog_system.entity.Notification; // 引入 Notification
import com.blog.blog_system.mapper.*; // 简化导入
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class BlogService {

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private VisitLogMapper visitLogMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private NotificationMapper notificationMapper; // 注入通知 Mapper

    @Autowired
    private UserMapper userMapper; // 注入 UserMapper 用于查询昵称

    // 保留 getAllBlogs, saveBlog, searchBlogs 等基础 CRUD 方法
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
     * ✨✨✨ 混合推荐引擎 v2.0 ✨✨✨
     * 策略优先级：协同过滤 > 内容标签 > 全站热门
     */
    public List<Blog> getPersonalizedBlogs(Long userId) {
        // 使用 LinkedHashMap 去重并保持插入顺序
        Map<Long, Blog> recommendMap = new LinkedHashMap<>();

        // 1. 【协同过滤】推荐 (User-Based CF)
        // 逻辑：寻找口味相似用户喜欢的文章
        List<Blog> cfBlogs = blogMapper.findCollaborativeBlogs(userId);
        if (cfBlogs != null) {
            for (Blog b : cfBlogs) {
                recommendMap.put(b.getId(), b);
            }
        }

        // 2. 【内容画像】推荐 (Content-Based)
        // 逻辑：如果协同过滤不够10篇，用标签画像补齐
        if (recommendMap.size() < 10) {
            List<String> viewedTags = visitLogMapper.selectViewedTags(userId);
            List<String> likedTags = visitLogMapper.selectLikedTags(userId);
            List<String> collectedTags = visitLogMapper.selectCollectedTags(userId);

            if (!viewedTags.isEmpty() || !likedTags.isEmpty() || !collectedTags.isEmpty()) {
                Map<String, Integer> tagScores = new HashMap<>();
                calculateScore(tagScores, viewedTags, 1); // 阅读+1分
                calculateScore(tagScores, likedTags, 3);  // 点赞+3分
                calculateScore(tagScores, collectedTags, 5); // 收藏+5分

                // 找出得分最高的标签
                String topTag = "";
                int maxScore = 0;
                for (Map.Entry<String, Integer> entry : tagScores.entrySet()) {
                    if (entry.getValue() > maxScore) {
                        maxScore = entry.getValue();
                        topTag = entry.getKey();
                    }
                }

                if (!topTag.isEmpty()) {
                    List<Blog> tagBlogs = blogMapper.findBlogsByTag(topTag);
                    for (Blog b : tagBlogs) {
                        if (!recommendMap.containsKey(b.getId())) {
                            recommendMap.put(b.getId(), b);
                        }
                    }
                }
            }
        }

        // 3. 【热门兜底】推荐 (Hot Fallback)
        // 逻辑：如果还不够10篇，用全站热门补齐
        if (recommendMap.size() < 10) {
            List<Blog> hotBlogs = blogMapper.findHotBlogs();
            for (Blog b : hotBlogs) {
                if (!recommendMap.containsKey(b.getId())) {
                    recommendMap.put(b.getId(), b);
                }
            }
        }

        return new ArrayList<>(recommendMap.values());
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

    /**
     * ✨✨✨ 点赞逻辑 (已修复：增加消息通知) ✨✨✨
     */
    @Transactional(rollbackFor = Exception.class)
    public String toggleLike(Long blogId, Long userId) {
        int count = blogMapper.checkIsLiked(userId, blogId);
        if (count == 0) {
            try {
                blogMapper.addLike(userId, blogId);
                blogMapper.incrementLikes(blogId);

                // ✨ 触发通知：类型1=点赞
                sendNotification(userId, blogId, 1, "点赞了你的文章");

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

    // 辅助：更新平均分
    private void updateBlogAverageScore(Long blogId) {
        Double avgScore = commentMapper.calculateAvgScore(blogId);
        if (avgScore != null) {
            double formattedScore = (double) Math.round(avgScore * 10) / 10;
            blogMapper.updateScore(blogId, formattedScore);
        }
    }

    /**
     * ✨✨✨ 评论逻辑 (已修复：增加消息通知) ✨✨✨
     */
    @Transactional(rollbackFor = Exception.class)
    public String addComment(Comment comment) {
        // 1. 如果有评分，先清空旧分
        if (comment.getScore() != null && comment.getScore() > 0) {
            commentMapper.clearPreviousScores(comment.getUserId(), comment.getBlogId());
        }

        // 2. 插入评论
        commentMapper.insert(comment);

        // 3. 更新文章平均分
        if (comment.getScore() != null && comment.getScore() > 0) {
            updateBlogAverageScore(comment.getBlogId());
        }

        // ✨ 触发通知：类型2=评论
        String msg = (comment.getContent() == null || comment.getContent().isEmpty())
                ? "给你的文章打了分"
                : "评论: " + comment.getContent();
        // 截取过长内容
        if (msg.length() > 20) msg = msg.substring(0, 20) + "...";

        sendNotification(comment.getUserId(), comment.getBlogId(), 2, msg);

        return "操作成功";
    }

    @Transactional(rollbackFor = Exception.class)
    public String deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.findById(commentId);
        if (comment == null) return "评论不存在";
        if (!comment.getUserId().equals(userId)) {
            return "无权删除";
        }
        commentMapper.deleteById(commentId);
        if (comment.getScore() != null && comment.getScore() > 0) {
            updateBlogAverageScore(comment.getBlogId());
        }
        return "删除成功";
    }

    /**
     * ✨✨✨ 辅助方法：构造并发送通知 ✨✨✨
     */
    private void sendNotification(Long senderId, Long blogId, Integer type, String content) {
        // 1. 查博客详情，获取作者信息
        Blog blog = blogMapper.findById(blogId);
        if (blog == null) return;

        // 2. 根据作者昵称反查作者ID (因为目前blog表只存了author昵称)
        User author = userMapper.findByUsername(blog.getAuthor());
        if (author == null) return;

        // 3. 自己操作自己不发通知
        if (author.getId().equals(senderId)) return;

        // 4. 查发送者信息
        User sender = userMapper.findById(senderId);

        // 5. 插入通知
        Notification notify = new Notification();
        notify.setUserId(author.getId()); // 接收人：文章作者
        notify.setSenderId(senderId);     // 发送人：当前操作用户
        notify.setSenderName(sender.getNickname() == null ? sender.getUsername() : sender.getNickname());
        notify.setType(type);
        notify.setContent(content);
        notify.setRelatedId(blogId);

        notificationMapper.insert(notify);
    }
}