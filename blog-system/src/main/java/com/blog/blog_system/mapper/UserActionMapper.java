package com.blog.blog_system.mapper;

import com.blog.blog_system.entity.Blog;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserActionMapper {

    // 1. 检查是否存在某种操作 (比如：是否已收藏？)
    @Select("SELECT COUNT(*) FROM user_action WHERE user_id = #{userId} AND blog_id = #{blogId} AND type = #{type}")
    int checkExist(Long userId, Long blogId, Integer type);

    // 2. 添加操作 (收藏/拉黑/待读)
    @Insert("INSERT INTO user_action(user_id, blog_id, type, create_time) VALUES(#{userId}, #{blogId}, #{type}, NOW())")
    void add(Long userId, Long blogId, Integer type);

    // 3. 移除操作 (取消收藏/移除拉黑)
    @Delete("DELETE FROM user_action WHERE user_id = #{userId} AND blog_id = #{blogId} AND type = #{type}")
    void remove(Long userId, Long blogId, Integer type);

    // 4. 查询列表 (比如：查询我收藏的所有博客)
    // 这是一个多表查询：从 user_action 表找 ID，去 blog 表查详情
    @Select("SELECT b.* FROM blog b " +
            "JOIN user_action ua ON b.id = ua.blog_id " +
            "WHERE ua.user_id = #{userId} AND ua.type = #{type} " +
            "ORDER BY ua.create_time DESC")
    List<Blog> selectBlogsByType(Long userId, Integer type);
}