package com.blog.blog_system.service;

import com.blog.blog_system.entity.User;
import com.blog.blog_system.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户业务逻辑层
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 注册逻辑
     */
    public String register(User user) {
        User existUser = userMapper.findByUsername(user.getUsername());
        if (existUser != null) {
            return "注册失败：用户名已被占用！";
        }
        userMapper.insert(user);
        return "注册成功！";
    }

    /**
     * 登录逻辑 (新增加的部分)
     */
    public String login(User user) {
        // 1. 先根据用户名去数据库查有没有这个人
        User existUser = userMapper.findByUsername(user.getUsername());

        // 2. 如果没查到，说明没注册过
        if (existUser == null) {
            return "登录失败：用户不存在！";
        }

        // 3. 如果查到了，比对密码对不对（目前是明文比对，后续我们可以升级为加密）
        if (!existUser.getPassword().equals(user.getPassword())) {
            return "登录失败：密码错误！";
        }

        // 4. 全部校验通过
        return "登录成功！";
    }
}