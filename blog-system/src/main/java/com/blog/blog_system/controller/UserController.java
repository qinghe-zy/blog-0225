package com.blog.blog_system.controller;

import com.blog.blog_system.entity.User;
import com.blog.blog_system.mapper.UserMapper;
import com.blog.blog_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.blog.blog_system.mapper.VisitLogMapper;

@RestController
@RequestMapping("/api/user")
@CrossOrigin // 允许跨域
public class UserController {

    @Autowired
    private UserService userService;

    // 在这里注入 UserMapper
    @Autowired
    private UserMapper userMapper;

    // ✨✨ 新增：注入 VisitLogMapper，用于查询阅读时长
    @Autowired
    private VisitLogMapper visitLogMapper;

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        return userService.register(user);
    }

    /**
     * 登录接口
     * 访问网址: POST http://localhost:8080/api/user/login
     */
    @PostMapping("/login")
    public Object login(@RequestBody User loginUser) {
        // 直接使用上面注入好的 userMapper
        User dbUser = userMapper.findByUsername(loginUser.getUsername());

        // 2. 判断用户是否存在，并且密码是否对得上
        if (dbUser != null && dbUser.getPassword().equals(loginUser.getPassword())) {
            // 关键点：密码对上了，把整个用户数据（ID、用户名、头像等）传回给前端
            // 为了安全，把密码抹掉再返回
            dbUser.setPassword(null);
            return dbUser;
        }

        // 3. 失败返回 null 或者错误提示
        return "账号或密码错误";
    }

    // 修改个人信息接口: PUT http://localhost:8080/api/user/update
    @PutMapping("/update")
    public String update(@RequestBody User user) {
        // 1. 先查出数据库里原来的旧数据
        User oldUser = userMapper.findById(user.getId());

        // 2. 只有当用户填了新密码时，才更新密码
        // 如果前端传在这个字段是空的，就说明用户不想改密码，我们把旧密码填回去
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(oldUser.getPassword());
        }

        // 3. 强制锁定账号名 (防止有人通过这个接口恶意改登录名)
        user.setUsername(oldUser.getUsername());

        // 4. 执行更新
        userMapper.update(user);
        return "修改成功！";
    }

    /**
     * ✨✨ 新增：获取用户统计数据接口 ✨✨
     * 作用：在个人中心显示“累计学习时长”
     * GET http://localhost:8080/api/user/stats?userId=1
     */
    @GetMapping("/stats")
    public Integer getStats(@RequestParam Long userId) {
        // 调用 Mapper 查询该用户的总阅读时长（秒）
        return visitLogMapper.sumDurationByUserId(userId);
    }
}