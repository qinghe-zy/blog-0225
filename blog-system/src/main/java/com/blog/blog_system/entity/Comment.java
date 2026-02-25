package com.blog.blog_system.entity;

import lombok.Data;

@Data
public class Comment {
    private Long id;
    private String content;
    private String createTime;
    private Long userId;
    private String username;
    private Long blogId;
    // 新增：评分字段 (1.0 - 5.0)
    private Double score;
    // 新增：头像字段
    private String avatar;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getBlogId() { return blogId; }
    public void setBlogId(Long blogId) { this.blogId = blogId; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}