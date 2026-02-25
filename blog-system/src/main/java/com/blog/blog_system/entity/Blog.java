package com.blog.blog_system.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Blog {
    private Long id;
    private String title;
    private String content;
    private String category;
    private String tags;
    private String author;
    private String url;
    private LocalDateTime createTime;
    private String summary; // 摘要
    private Integer views;  // 浏览量
}