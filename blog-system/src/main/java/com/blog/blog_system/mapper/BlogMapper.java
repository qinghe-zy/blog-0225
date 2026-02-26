package com.blog.blog_system.mapper;

import com.blog.blog_system.entity.Blog;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Set; // ✨ 修复：补充 Set 集合类的导入

@Mapper
public interface BlogMapper {

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

    // ================= 点赞相关 =================

    @Select("SELECT COUNT(*) FROM user_like WHERE user_id = #{userId} AND blog_id = #{blogId}")
    int checkIsLiked(@Param("userId") Long userId, @Param("blogId") Long blogId);

    @Insert("INSERT INTO user_like(user_id, blog_id, create_time) VALUES(#{userId}, #{blogId}, NOW())")
    void addLike(@Param("userId") Long userId, @Param("blogId") Long blogId);

    @Delete("DELETE FROM user_like WHERE user_id = #{userId} AND blog_id = #{blogId}")
    void removeLike(@Param("userId") Long userId, @Param("blogId") Long blogId);

    @Update("UPDATE blog SET likes = likes + 1 WHERE id = #{id}")
    void incrementLikes(Long id);

    @Update("UPDATE blog SET likes = likes - 1 WHERE id = #{id} AND likes > 0")
    void decrementLikes(Long id);

    // ================= 基础查询 =================

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

    // ================== ✨ 状态查询 (新增) ==================

    @Select("SELECT blog_id FROM user_like WHERE user_id = #{userId}")
    Set<Long> findLikedBlogIds(Long userId);

    @Select("SELECT blog_id FROM user_action WHERE user_id = #{userId} AND type = 1")
    Set<Long> findCollectedBlogIds(Long userId);

    // ================== ✨ 全局过滤拉黑数据 (type = 3) ==================

    // 1. 全部文章 (排除拉黑)
    @Select("SELECT * FROM blog WHERE id NOT IN (SELECT blog_id FROM user_action WHERE user_id = #{userId} AND type = 3) ORDER BY create_time DESC")
    List<Blog> findAllWithFilter(@Param("userId") Long userId);

    // 2. 搜索文章 (排除拉黑)
    @Select("SELECT * FROM blog WHERE (title LIKE CONCAT('%', #{keyword}, '%') OR tags LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND id NOT IN (SELECT blog_id FROM user_action WHERE user_id = #{userId} AND type = 3) ORDER BY create_time DESC")
    List<Blog> searchWithFilter(@Param("keyword") String keyword, @Param("userId") Long userId);

    // 3. 热门兜底 (排除拉黑)
    @Select("SELECT * FROM blog WHERE id NOT IN (SELECT blog_id FROM user_action WHERE user_id = #{userId} AND type = 3) ORDER BY views DESC LIMIT 5")
    List<Blog> findHotBlogsWithFilter(@Param("userId") Long userId);

    // 4. 协同过滤推荐 (融合了“点赞+收藏加权”与“拉黑屏蔽”)
    @Select("SELECT b.*, SUM(w.weight) as rec_score " +
            "FROM blog b " +
            "JOIN ( " +
            "    SELECT distinct target_id as blog_id, user_id, 3 as weight FROM user_like " +
            "    UNION ALL " +
            "    SELECT distinct blog_id, user_id, 5 as weight FROM user_action WHERE type = 1 " +
            ") target_behavior ON b.id = target_behavior.blog_id " +
            "JOIN ( " +
            "    SELECT t2.user_id " +
            "    FROM ( " +
            "        SELECT target_id as blog_id FROM user_like WHERE user_id = #{userId} " +
            "        UNION ALL " +
            "        SELECT blog_id FROM user_action WHERE user_id = #{userId} AND type = 1 " +
            "    ) my_history " +
            "    JOIN ( " +
            "        SELECT target_id as blog_id, user_id FROM user_like " +
            "        UNION ALL " +
            "        SELECT blog_id, user_id FROM user_action WHERE type = 1 " +
            "    ) t2 ON my_history.blog_id = t2.blog_id " +
            "    WHERE t2.user_id != #{userId} " +
            ") neighbor ON target_behavior.user_id = neighbor.user_id " +
            "WHERE b.id NOT IN ( " +
            "    SELECT target_id FROM user_like WHERE user_id = #{userId} " + // 排除已赞
            "    UNION " +
            "    SELECT blog_id FROM user_action WHERE user_id = #{userId} AND type = 1 " + // 排除已收藏
            "    UNION " +
            "    SELECT blog_id FROM user_action WHERE user_id = #{userId} AND type = 3 " + // ✨✨ 屏蔽已拉黑
            ") " +
            "GROUP BY b.id " +
            "ORDER BY rec_score DESC " +
            "LIMIT 10")
    List<Blog> findCollaborativeBlogs(Long userId);
}