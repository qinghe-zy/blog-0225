package com.blog.blog_system.mapper;

import com.blog.blog_system.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户表的数据访问层 (操作数据库)
 */
@Mapper
public interface UserMapper {

    // 根据用户名查询完整用户信息
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    @Insert("INSERT INTO user(username, password, email, create_time) " +
            "VALUES(#{username}, #{password}, #{email}, NOW())")
    void insert(User user);
    //新增：更新用户信息（昵称、密码等）
    @Update("UPDATE user SET nickname=#{nickname}, password=#{password} WHERE id=#{id}")
    void update(User user);
    //新增：根据 ID 查用户 (为了获取旧密码)
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);
}