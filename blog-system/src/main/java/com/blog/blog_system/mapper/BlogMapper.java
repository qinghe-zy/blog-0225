package com.blog.blog_system.mapper;

import com.blog.blog_system.entity.Blog;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface BlogMapper {

    // ... (保留 findAll, insert, search, deleteById, findById, update, updateScore, incrementViews 方法) ...
    // 为节省篇幅，这里省略未修改的方法，请保持原样

    // ================= 重点修复区域 =================

    // 检查是否点赞
    @Select("SELECT COUNT(*) FROM user_like WHERE user_id = #{userId} AND blog_id = #{blogId}")
    int checkIsLiked(@Param("userId") Long userId, @Param("blogId") Long blogId);

    // 添加点赞记录
    @Insert("INSERT INTO user_like(user_id, blog_id, create_time) VALUES(#{userId}, #{blogId}, NOW())")
    void addLike(@Param("userId") Long userId, @Param("blogId") Long blogId);

    // 移除点赞记录
    @Delete("DELETE FROM user_like WHERE user_id = #{userId} AND blog_id = #{blogId}")
    void removeLike(@Param("userId") Long userId, @Param("blogId") Long blogId);

    // 点赞数 +1
    @Update("UPDATE blog SET likes = likes + 1 WHERE id = #{id}")
    void incrementLikes(Long id);

    // ✨✨✨ 修复：点赞数 -1 (增加双重保险：只有当 likes > 0 时才减) ✨✨✨
    @Update("UPDATE blog SET likes = likes - 1 WHERE id = #{id} AND likes > 0")
    void decrementLikes(Long id);

    // ================= 结束重点修复区域 =================

    // ... (保留 findLikedBlogs, findHotBlogs, findRecentBlogs 等查询方法) ...
    // 请确保其他 Mapper 方法保持原样
    @Select("SELECT * FROM blog ORDER BY create_time DESC")
    List<Blog> findAll();

    @Insert("INSERT INTO blog(title, content, category, summary, tags, url, views, likes, author, create_time) " +
            "VALUES(#{title}, #{content}, #{category}, #{summary}, #{tags}, #{url}, 0, 0, #{author}, NOW())")
    void insert(Blog blog);

    @Select("SELECT * FROM blog WHERE title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR category LIKE CONCAT('%', #{keyword}, '%') " +
            "OR tags LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY create_time DESC")
    List<Blog> search(String keyword);

    @Delete("DELETE FROM blog WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT * FROM blog WHERE id = #{id}")
    Blog findById(Long id);

    @Update("UPDATE blog SET title=#{title}, content=#{content}, category=#{category}, tags=#{tags}, url=#{url}, summary=#{summary} WHERE id=#{id}")
    void update(Blog blog);

    @Update("UPDATE blog SET score = #{score} WHERE id = #{id}")
    void updateScore(@Param("id") Long id, @Param("score") Double score);

    @Update("UPDATE blog SET views = views + 1 WHERE id = #{id}")
    void incrementViews(Long id);

    @Select("SELECT b.* FROM blog b JOIN user_like ul ON b.id = ul.blog_id WHERE ul.user_id = #{userId} ORDER BY ul.create_time DESC")
    List<Blog> findLikedBlogs(Long userId);

    @Select("SELECT * FROM blog ORDER BY views DESC LIMIT 5")
    List<Blog> findHotBlogs();

    @Select("SELECT b.* FROM blog b JOIN visit_log v ON b.id = v.blog_id WHERE v.user_id = #{userId} GROUP BY b.id ORDER BY MAX(v.create_time) DESC LIMIT 10")
    List<Blog> findRecentBlogs(Long userId);

    @Update("UPDATE blog SET collects = collects + 1 WHERE id = #{id}")
    void incrementCollects(Long id);

    @Update("UPDATE blog SET collects = collects - 1 WHERE id = #{id}")
    void decrementCollects(Long id);

    @Select("SELECT * FROM blog WHERE id != #{id} AND tags LIKE CONCAT('%', #{keyword}, '%') ORDER BY views DESC LIMIT 5")
    List<Blog> findRelatedBlogs(@Param("id") Long id, @Param("keyword") String keyword);

    @Select("SELECT * FROM blog WHERE tags LIKE CONCAT('%', #{tag}, '%') ORDER BY views DESC LIMIT 10")
    List<Blog> findBlogsByTag(String tag);

    @Select("SELECT b.*, COUNT(distinct ul_others.user_id) as rec_weight FROM blog b JOIN user_like ul_others ON b.id = ul_others.blog_id JOIN user_like ul_me ON ul_others.user_id = ul_me.user_id WHERE ul_me.user_id != #{userId} AND ul_me.blog_id IN (SELECT blog_id FROM user_like WHERE user_id = #{userId}) AND b.id NOT IN (SELECT blog_id FROM user_like WHERE user_id = #{userId}) GROUP BY b.id ORDER BY rec_weight DESC LIMIT 10")
    List<Blog> findCollaborativeBlogs(Long userId);
}