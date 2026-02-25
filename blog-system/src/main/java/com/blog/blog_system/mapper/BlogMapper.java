package com.blog.blog_system.mapper;

import com.blog.blog_system.entity.Blog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BlogMapper {

    // 1. 查询所有博客
    @Select("SELECT * FROM blog ORDER BY create_time DESC")
    List<Blog> findAll();

    // 2. 插入新博客
    @Insert("INSERT INTO blog(title, content, category, summary, tags, url, views, likes, author, create_time) " +
            "VALUES(#{title}, #{content}, #{category}, #{summary}, #{tags}, #{url}, 0, 0, #{author}, NOW())")
    void insert(Blog blog);

    // 3. 搜索
    @Select("SELECT * FROM blog WHERE title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR category LIKE CONCAT('%', #{keyword}, '%') " +
            "OR tags LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY create_time DESC")
    List<Blog> search(String keyword);

    // 4. 删除
    @Delete("DELETE FROM blog WHERE id = #{id}")
    void deleteById(Long id);

    // 5. 详情
    @Select("SELECT * FROM blog WHERE id = #{id}")
    Blog findById(Long id);

    // 6. 更新
    @Update("UPDATE blog SET title=#{title}, content=#{content}, category=#{category}, tags=#{tags}, url=#{url}, summary=#{summary} WHERE id=#{id}")
    void update(Blog blog);

    // 浏览量+1
    @Update("UPDATE blog SET views = views + 1 WHERE id = #{id}")
    void incrementViews(Long id);

    // --- 点赞相关 ---
    @Select("SELECT COUNT(*) FROM user_like WHERE user_id = #{userId} AND blog_id = #{blogId}")
    int checkIsLiked(@Param("userId") Long userId, @Param("blogId") Long blogId);

    @Insert("INSERT INTO user_like(user_id, blog_id, create_time) VALUES(#{userId}, #{blogId}, NOW())")
    void addLike(@Param("userId") Long userId, @Param("blogId") Long blogId);

    @Delete("DELETE FROM user_like WHERE user_id = #{userId} AND blog_id = #{blogId}")
    void removeLike(@Param("userId") Long userId, @Param("blogId") Long blogId);

    @Update("UPDATE blog SET likes = likes + 1 WHERE id = #{id}")
    void incrementLikes(Long id);

    @Update("UPDATE blog SET likes = likes - 1 WHERE id = #{id}")
    void decrementLikes(Long id);

    // --- 列表查询 ---

    // 我的点赞
    @Select("SELECT b.* FROM blog b " +
            "JOIN user_like ul ON b.id = ul.blog_id " +
            "WHERE ul.user_id = #{userId} " +
            "ORDER BY ul.create_time DESC")
    List<Blog> findLikedBlogs(Long userId);

    // 全站热门
    @Select("SELECT * FROM blog ORDER BY views DESC LIMIT 5")
    List<Blog> findHotBlogs();

    // 最近浏览
    @Select("SELECT b.* FROM blog b " +
            "JOIN visit_log v ON b.id = v.blog_id " +
            "WHERE v.user_id = #{userId} " +
            "GROUP BY b.id " +
            "ORDER BY MAX(v.create_time) DESC LIMIT 10")
    List<Blog> findRecentBlogs(Long userId);

    @Update("UPDATE blog SET collects = collects + 1 WHERE id = #{id}")
    void incrementCollects(Long id);

    @Update("UPDATE blog SET collects = collects - 1 WHERE id = #{id}")
    void decrementCollects(Long id);

    // 新增：相关推荐查询
    // 逻辑：查找包含指定标签(keyword)的文章，排除当前这篇(id)，按阅读量排序取前5
    @Select("SELECT * FROM blog WHERE id != #{id} " +
            "AND tags LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY views DESC LIMIT 5")
    List<Blog> findRelatedBlogs(@Param("id") Long id, @Param("keyword") String keyword);
}