package com.blog.blog_system.controller;

import com.blog.blog_system.entity.User;
import com.blog.blog_system.mapper.UserMapper;
import com.blog.blog_system.service.UserService;
import com.blog.blog_system.utils.MD5Util; // ✨ 引入 MD5 工具
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.blog.blog_system.mapper.VisitLogMapper;
import java.util.Map;

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
        // 调用 Service，Service 里已经处理了加密
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

        // ✨✨ 安全升级：先将用户输入的密码加密
        String inputMd5 = MD5Util.encrypt(loginUser.getPassword());

        // 2. 判断用户是否存在，并且密码是否对得上 (使用加密后的密码比对)
        if (dbUser != null && dbUser.getPassword().equals(inputMd5)) {
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
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            // ✨✨ 安全升级：修改密码也要加密
            user.setPassword(MD5Util.encrypt(user.getPassword()));
        } else {
            // 如果前端传在这个字段是空的，就说明用户不想改密码，我们把旧密码填回去
            user.setPassword(oldUser.getPassword());
        }

        // 3. 强制锁定账号名 (防止有人通过这个接口恶意改登录名)
        user.setUsername(oldUser.getUsername());

        // ✨✨ 补充：如果前端没传头像 (avatar为null)，保持旧头像不变
        if (user.getAvatar() == null) {
            user.setAvatar(oldUser.getAvatar());
        }

        // 4. 执行更新 (Mapper 已经更新支持 avatar)
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
    /**
     * ✨✨✨ 新增：获取雷达图数据 ✨✨✨
     * GET /api/user/radar?userId=1
     */
    @GetMapping("/radar")
    public Map<String, Object> getRadar(@RequestParam Long userId) {
        return userService.getUserRadar(userId);
    }
}