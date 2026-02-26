package com.blog.blog_system.controller;

import com.blog.blog_system.common.Result; // ✨ 引入 Result
import com.blog.blog_system.entity.Notification;
import com.blog.blog_system.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notification")
@CrossOrigin
public class NotificationController {

    @Autowired
    private NotificationMapper notificationMapper;

    @GetMapping("/list")
    public Result<List<Notification>> list(@RequestParam Long userId) {
        return Result.success(notificationMapper.findByUserId(userId));
    }

    @GetMapping("/count")
    public Result<Integer> count(@RequestParam Long userId) {
        return Result.success(notificationMapper.countUnread(userId));
    }

    @PostMapping("/read")
    public Result<String> read(@RequestParam Long id) {
        notificationMapper.markAsRead(id);
        return Result.success("已读");
    }

    @PostMapping("/read-all")
    public Result<String> readAll(@RequestParam Long userId) {
        notificationMapper.markAllAsRead(userId);
        return Result.success("全部已读");
    }
}