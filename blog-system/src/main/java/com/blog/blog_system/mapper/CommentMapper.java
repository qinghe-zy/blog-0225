package com.blog.blog_system.mapper;

import com.blog.blog_system.entity.Comment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CommentMapper {
    // 1. 发表评论 (✨✨ 修改：增加了 score 字段插入)
    @Insert("INSERT INTO comment(content, user_id, username, blog_id, score, create_time) " +
            "VALUES(#{content}, #{userId}, #{username}, #{blogId}, #{score}, NOW())")
    void insert(Comment comment);

    // 2. 根据博客ID查评论 (按时间倒序)
    @Select("SELECT * FROM comment WHERE blog_id = #{blogId} ORDER BY create_time DESC")
    List<Comment> findByBlogId(Long blogId);

    // ✨✨ 新增：计算某篇博客的平均分 (只计算大于0的评分)
    @Select("SELECT IFNULL(AVG(score), 0) FROM comment WHERE blog_id = #{blogId} AND score > 0")
    Double calculateAvgScore(Long blogId);
}