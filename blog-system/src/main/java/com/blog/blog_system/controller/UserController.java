package com.blog.blog_system.controller;

import com.blog.blog_system.common.Result; // 引入 Result
import com.blog.blog_system.entity.User;
import com.blog.blog_system.mapper.UserMapper;
import com.blog.blog_system.service.UserService;
import com.blog.blog_system.utils.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.blog.blog_system.mapper.VisitLogMapper;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VisitLogMapper visitLogMapper;

    //修改返回值: Result<String>
    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        String msg = userService.register(user);
        if (msg.contains("成功")) {
            return Result.success(null, msg);
        } else {
            return Result.error(msg);
        }
    }

    /**
     * 登录接口
     */
    //修改返回值: Result<User>
    @PostMapping("/login")
    public Result<User> login(@RequestBody User loginUser) {
        User dbUser = userMapper.findByUsername(loginUser.getUsername());
        String inputMd5 = MD5Util.encrypt(loginUser.getPassword());

        if (dbUser != null && dbUser.getPassword().equals(inputMd5)) {
            dbUser.setPassword(null); // 脱敏
            return Result.success(dbUser, "登录成功");
        }
        return Result.error("账号或密码错误");
    }

    //修改返回值: Result<String>
    @PutMapping("/update")
    public Result<String> update(@RequestBody User user) {
        User oldUser = userMapper.findById(user.getId());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(MD5Util.encrypt(user.getPassword()));
        } else {
            user.setPassword(oldUser.getPassword());
        }
        user.setUsername(oldUser.getUsername());
        if (user.getAvatar() == null) {
            user.setAvatar(oldUser.getAvatar());
        }
        userMapper.update(user);
        return Result.success(null, "修改成功！");
    }

    //修改返回值: Result<Integer>
    @GetMapping("/stats")
    public Result<Integer> getStats(@RequestParam Long userId) {
        return Result.success(visitLogMapper.sumDurationByUserId(userId));
    }

    //修改返回值: Result<Map>
    @GetMapping("/radar")
    public Result<Map<String, Object>> getRadar(@RequestParam Long userId) {
        return Result.success(userService.getUserRadar(userId));
    }
}