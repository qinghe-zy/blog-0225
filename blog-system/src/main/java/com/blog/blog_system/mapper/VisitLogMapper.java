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

    // 新增：统计用户总阅读时长 (秒)
    // IFNULL 避免如果用户一条记录都没有时返回 null 报错
    @Select("SELECT IFNULL(SUM(duration), 0) FROM visit_log WHERE user_id = #{userId}")
    Integer sumDurationByUserId(Long userId);
    // 关联 blog 表，把用户看过的文章的 tags 字段全查出来
    @Select("SELECT b.tags FROM visit_log v " +
            "JOIN blog b ON v.blog_id = b.id " +
            "WHERE v.user_id = #{userId} AND b.tags IS NOT NULL AND b.tags != ''")
    List<String> selectViewedTags(Long userId);

}