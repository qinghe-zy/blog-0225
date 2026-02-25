package com.blog.blog_system.mapper;

import com.blog.blog_system.entity.Comment;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CommentMapper {

    // 1. 发表评论
    @Insert("INSERT INTO comment(content, user_id, username, avatar, blog_id, score, create_time) " +
            "VALUES(#{content}, #{userId}, #{username}, #{avatar}, #{blogId}, #{score}, NOW())")
    void insert(Comment comment);

    /**
     *修改：只查询有内容的评论 (过滤掉 user_only_rated)
     * 且按时间倒序排列
     */
    @Select("SELECT * FROM comment WHERE blog_id = #{blogId} " +
            "AND content IS NOT NULL AND content != '' " +
            "ORDER BY create_time DESC")
    List<Comment> findByBlogId(Long blogId);

    // 计算平均分 (只算大于0的)
    @Select("SELECT IFNULL(AVG(score), 0) FROM comment WHERE blog_id = #{blogId} AND score > 0")
    Double calculateAvgScore(Long blogId);

    /**
     * 新增：将该用户在此博客之前的评分清零
     * 逻辑：保证一个用户对一篇文章只有一个有效分数
     */
    @Update("UPDATE comment SET score = 0 WHERE user_id = #{userId} AND blog_id = #{blogId}")
    void clearPreviousScores(@Param("userId") Long userId, @Param("blogId") Long blogId);

    // 新增：删除评论
    @Delete("DELETE FROM comment WHERE id = #{id}")
    void deleteById(Long id);

    // 根据ID查评论 (用于校验权限)
    @Select("SELECT * FROM comment WHERE id = #{id}")
    Comment findById(Long id);
    /**
     * 查询指定用户对指定博客的最新评分
     * @param userId 用户ID
     * @param blogId 博客ID
     * @return 评分分值
     */
    @Select("SELECT score FROM comment WHERE user_id = #{userId} AND blog_id = #{blogId} " +
            "AND score > 0 ORDER BY create_time DESC LIMIT 1")
    Double getUserRawScore(@Param("userId") Long userId, @Param("blogId") Long blogId);
}