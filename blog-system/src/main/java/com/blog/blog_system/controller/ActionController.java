package com.blog.blog_system.controller;

import com.blog.blog_system.entity.Blog;
import com.blog.blog_system.mapper.BlogMapper;
import com.blog.blog_system.mapper.UserActionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/action")
@CrossOrigin
public class ActionController {

    @Autowired
    private UserActionMapper userActionMapper;

    @Autowired
    private BlogMapper blogMapper;

    /**
     * 统一动作接口：切换状态 (Toggle)
     * POST /api/action/toggle?userId=1&blogId=2&type=1
     * type定义: 1=收藏, 2=待读, 3=拉黑
     */
    @PostMapping("/toggle")
    public String toggle(@RequestParam Long userId,
                         @RequestParam Long blogId,
                         @RequestParam Integer type) {

        // 1. 检查是否已经操作过
        int count = userActionMapper.checkExist(userId, blogId, type);

        if (count > 0) {
            // 存在 -> 执行“取消”逻辑
            userActionMapper.remove(userId, blogId, type);

            // 如果是收藏(type=1)，还要把博客的收藏数 -1
            if (type == 1) {
                blogMapper.decrementCollects(blogId);
            }
            return "已取消";
        } else {
            // 不存在 -> 执行“添加”逻辑
            userActionMapper.add(userId, blogId, type);

            // 如果是收藏(type=1)，博客收藏数 +1
            if (type == 1) {
                blogMapper.incrementCollects(blogId);
            }
            return "操作成功";
        }
    }

    /**
     * 检查状态接口 (用于前端判断按钮高亮)
     * GET /api/action/check?userId=1&blogId=2&type=1
     */
    @GetMapping("/check")
    public Boolean check(@RequestParam Long userId,
                         @RequestParam Long blogId,
                         @RequestParam Integer type) {
        return userActionMapper.checkExist(userId, blogId, type) > 0;
    }

    /**
     * 获取列表接口 (我的书架、我的黑名单)
     * GET /api/action/list?userId=1&type=1
     */
    @GetMapping("/list")
    public List<Blog> list(@RequestParam Long userId,
                           @RequestParam Integer type) {
        return userActionMapper.selectBlogsByType(userId, type);
    }

}