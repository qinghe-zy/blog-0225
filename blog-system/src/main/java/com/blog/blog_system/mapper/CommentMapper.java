package com.blog.blog_system.mapper;

import com.blog.blog_system.entity.Comment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CommentMapper {
    // 1. 发表评论
    @Insert("INSERT INTO comment(content, user_id, username, blog_id, create_time) " +
            "VALUES(#{content}, #{userId}, #{username}, #{blogId}, NOW())")
    void insert(Comment comment);

    // 2. 根据博客ID查评论 (按时间倒序)
    @Select("SELECT * FROM comment WHERE blog_id = #{blogId} ORDER BY create_time DESC")
    List<Comment> findByBlogId(Long blogId);
}