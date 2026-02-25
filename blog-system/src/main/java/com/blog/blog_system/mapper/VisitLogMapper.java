package com.blog.blog_system.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface VisitLogMapper {

    // 插入浏览记录
    @Insert("INSERT INTO visit_log(user_id, blog_id, create_time) VALUES(#{userId}, #{blogId}, NOW())")
    void insert(@Param("userId") Long userId, @Param("blogId") Long blogId);

    // 更新阅读时长
    @Update("UPDATE visit_log SET duration = duration + #{seconds} " +
            "WHERE id = (SELECT id FROM (SELECT id FROM visit_log " +
            "WHERE user_id = #{userId} AND blog_id = #{blogId} " +
            "ORDER BY create_time DESC LIMIT 1) as t)")
    void updateDuration(@Param("userId") Long userId,
                        @Param("blogId") Long blogId,
                        @Param("seconds") Integer seconds);

    // 统计总时长
    @Select("SELECT IFNULL(SUM(duration), 0) FROM visit_log WHERE user_id = #{userId}")
    Integer sumDurationByUserId(Long userId);

    // 查询看过的标签
    @Select("SELECT b.tags FROM visit_log v " +
            "JOIN blog b ON v.blog_id = b.id " +
            "WHERE v.user_id = #{userId} AND b.tags IS NOT NULL AND b.tags != ''")
    List<String> selectViewedTags(Long userId);

    /**
     * ✨✨✨ 新增：查询用户点赞过的文章标签 ✨✨✨
     * 关联 user_like 表和 blog 表
     */
    @Select("SELECT b.tags FROM user_like ul " +
            "JOIN blog b ON ul.blog_id = b.id " +
            "WHERE ul.user_id = #{userId} AND b.tags IS NOT NULL AND b.tags != ''")
    List<String> selectLikedTags(Long userId);

    /**
     * ✨✨✨ 新增：查询用户收藏过的文章标签 ✨✨✨
     * 关联 user_action 表 (type=1为收藏) 和 blog 表
     */
    @Select("SELECT b.tags FROM user_action ua " +
            "JOIN blog b ON ua.blog_id = b.id " +
            "WHERE ua.user_id = #{userId} AND ua.type = 1 AND b.tags IS NOT NULL AND b.tags != ''")
    List<String> selectCollectedTags(Long userId);
}