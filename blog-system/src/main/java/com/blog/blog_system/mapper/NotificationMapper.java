package com.blog.blog_system.mapper;

import com.blog.blog_system.entity.Notification;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface NotificationMapper {

    // 插入通知
    @Insert("INSERT INTO notification(user_id, sender_id, sender_name, type, content, related_id, is_read, create_time) " +
            "VALUES(#{userId}, #{senderId}, #{senderName}, #{type}, #{content}, #{relatedId}, 0, NOW())")
    void insert(Notification notification);

    // 查询我的通知 (按时间倒序)
    @Select("SELECT * FROM notification WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Notification> findByUserId(Long userId);

    // 标记单条已读
    @Update("UPDATE notification SET is_read = 1 WHERE id = #{id}")
    void markAsRead(Long id);

    // 一键已读
    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId}")
    void markAllAsRead(Long userId);

    // 查询未读数量
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0")
    int countUnread(Long userId);
}