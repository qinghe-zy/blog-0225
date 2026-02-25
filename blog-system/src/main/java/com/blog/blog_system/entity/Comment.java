package com.blog.blog_system.entity;

public class Comment {
    private Long id;
    private String content;
    private String createTime;
    private Long userId;
    private String username;
    private Long blogId;

    // 省略 Getter/Setter，如果你没装 Lombok 插件，请手动生成！
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
}