package com.blog.blog_system.entity;

import java.time.LocalDateTime;

public class UserAction {
    private Long id;
    private Long userId;
    private Long blogId;
    // 类型: 1=收藏, 2=待读, 3=拉黑
    private Integer type;
    private LocalDateTime createTime;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getBlogId() { return blogId; }
    public void setBlogId(Long blogId) { this.blogId = blogId; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}