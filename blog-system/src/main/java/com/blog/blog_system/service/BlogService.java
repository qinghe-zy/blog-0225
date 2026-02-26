package com.blog.blog_system.service;

import com.blog.blog_system.entity.Blog;
import com.blog.blog_system.entity.Comment;
import com.blog.blog_system.entity.User;
import com.blog.blog_system.entity.Notification;
import com.blog.blog_system.mapper.*;
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
    private NotificationMapper notificationMapper;

    @Autowired
    private UserMapper userMapper;

    // ✨✨ 核心更新：批量组装点赞与收藏状态 ✨✨
    public void populateInteractionStatus(List<Blog> blogs, Long userId) {
        if (userId == null || userId == 0L || blogs == null || blogs.isEmpty()) return;

        Set<Long> likedIds = blogMapper.findLikedBlogIds(userId);
        Set<Long> collectedIds = blogMapper.findCollectedBlogIds(userId);

        for (Blog blog : blogs) {
            blog.setIsLiked(likedIds.contains(blog.getId()));
            blog.setIsCollected(collectedIds.contains(blog.getId()));
        }
    }

    // ✨ 升级：过滤拉黑并附带状态
    public List<Blog> getAllBlogs(Long userId) {
        List<Blog> blogs = blogMapper.findAllWithFilter(userId);
        populateInteractionStatus(blogs, userId);
        return blogs;
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

    // ✨ 升级：过滤拉黑并附带状态
    public List<Blog> searchBlogs(String keyword, Long userId) {
        List<Blog> blogs = blogMapper.searchWithFilter(keyword, userId);
        populateInteractionStatus(blogs, userId);
        return blogs;
    }

    public String updateBlog(Blog blog) { blogMapper.update(blog); return "修改成功！"; }

    public void deleteBlog(Long id) { blogMapper.deleteById(id); }

    public Blog getBlogDetail(Long id, Long userId) {
        blogMapper.incrementViews(id);
        if (userId != null && userId != 0L) {
            try { visitLogMapper.insert(userId, id); }
            catch (Exception e) { System.err.println("记录浏览历史失败: " + e.getMessage()); }
        }
        return blogMapper.findById(id);
    }

    // ✨ 升级：过滤拉黑并附带状态
    public List<Blog> getHotBlogs(Long userId) {
        List<Blog> blogs = blogMapper.findHotBlogsWithFilter(userId);
        populateInteractionStatus(blogs, userId);
        return blogs;
    }

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

    // ✨ 升级：附带点赞/收藏状态
    public List<Blog> getPersonalizedBlogs(Long userId) {
        Map<Long, Blog> recommendMap = new LinkedHashMap<>();

        List<Blog> cfBlogs = blogMapper.findCollaborativeBlogs(userId);
        if (cfBlogs != null) {
            for (Blog b : cfBlogs) recommendMap.put(b.getId(), b);
        }

        if (recommendMap.size() < 10) {
            List<String> viewedTags = visitLogMapper.selectViewedTags(userId);
            List<String> likedTags = visitLogMapper.selectLikedTags(userId);
            List<String> collectedTags = visitLogMapper.selectCollectedTags(userId);

            if (!viewedTags.isEmpty() || !likedTags.isEmpty() || !collectedTags.isEmpty()) {
                Map<String, Integer> tagScores = new HashMap<>();
                calculateScore(tagScores, viewedTags, 1);
                calculateScore(tagScores, likedTags, 3);
                calculateScore(tagScores, collectedTags, 5);

                String topTag = "";
                int maxScore = 0;
                for (Map.Entry<String, Integer> entry : tagScores.entrySet()) {
                    if (entry.getValue() > maxScore) { maxScore = entry.getValue(); topTag = entry.getKey(); }
                }

                if (!topTag.isEmpty()) {
                    List<Blog> tagBlogs = blogMapper.findBlogsByTag(topTag);
                    for (Blog b : tagBlogs) {
                        if (!recommendMap.containsKey(b.getId())) recommendMap.put(b.getId(), b);
                    }
                }
            }
        }

        if (recommendMap.size() < 10) {
            List<Blog> hotBlogs = blogMapper.findHotBlogsWithFilter(userId); // 用过滤后的热门兜底
            for (Blog b : hotBlogs) {
                if (!recommendMap.containsKey(b.getId())) recommendMap.put(b.getId(), b);
            }
        }

        List<Blog> finalResult = new ArrayList<>(recommendMap.values());
        populateInteractionStatus(finalResult, userId); // ✨ 填充状态
        return finalResult;
    }

    private void calculateScore(Map<String, Integer> scores, List<String> tags, int weight) {
        if (tags == null) return;
        for (String tagStr : tags) {
            String[] splitTags = tagStr.split("[,，]");
            for (String t : splitTags) {
                t = t.trim();
                if (!t.isEmpty()) scores.put(t, scores.getOrDefault(t, 0) + weight);
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
                sendNotification(userId, blogId, 1, "点赞了你的文章");
                return "点赞成功";
            } catch (Exception e) { return "您已点赞"; }
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
        if (comment.getScore() != null && comment.getScore() > 0) {
            commentMapper.clearPreviousScores(comment.getUserId(), comment.getBlogId());
        }
        commentMapper.insert(comment);
        if (comment.getScore() != null && comment.getScore() > 0) {
            updateBlogAverageScore(comment.getBlogId());
        }
        String msg = (comment.getContent() == null || comment.getContent().isEmpty()) ? "给你的文章打了分" : "评论: " + comment.getContent();
        if (msg.length() > 20) msg = msg.substring(0, 20) + "...";
        sendNotification(comment.getUserId(), comment.getBlogId(), 2, msg);
        return "操作成功";
    }

    @Transactional(rollbackFor = Exception.class)
    public String deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.findById(commentId);
        if (comment == null) return "评论不存在";
        if (!comment.getUserId().equals(userId)) return "无权删除";
        commentMapper.deleteById(commentId);
        if (comment.getScore() != null && comment.getScore() > 0) {
            updateBlogAverageScore(comment.getBlogId());
        }
        return "删除成功";
    }

    private void sendNotification(Long senderId, Long blogId, Integer type, String content) {
        Blog blog = blogMapper.findById(blogId);
        if (blog == null) return;
        User author = userMapper.findByUsername(blog.getAuthor());
        if (author == null) return;
        if (author.getId().equals(senderId)) return;
        User sender = userMapper.findById(senderId);
        Notification notify = new Notification();
        notify.setUserId(author.getId());
        notify.setSenderId(senderId);
        notify.setSenderName(sender.getNickname() == null ? sender.getUsername() : sender.getNickname());
        notify.setType(type);
        notify.setContent(content);
        notify.setRelatedId(blogId);
        notificationMapper.insert(notify);
    }
}