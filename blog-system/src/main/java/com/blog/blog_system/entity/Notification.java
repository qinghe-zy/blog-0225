package com.blog.blog_system.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息通知实体类
 */
@Data
public class Notification {
    private Long id;
    private Long userId;      // 接收人
    private Long senderId;    // 发送人
    private String senderName;// 发送人昵称
    private Integer type;     // 1=点赞, 2=评论, 3=收藏
    private String content;   // 内容
    private Long relatedId;   // 关联ID
    private Integer isRead;   // 0=未读
    private LocalDateTime createTime;
}