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

    // ✨✨ 修改：插入时增加 avatar 字段 (nickname 注册时通常为空，暂不加入以保持你原有的精简风格)
    @Insert("INSERT INTO user(username, password, email, avatar, create_time) " +
            "VALUES(#{username}, #{password}, #{email}, #{avatar}, NOW())")
    void insert(User user);

    // ✨✨ 修改：更新时增加 avatar 字段
    @Update("UPDATE user SET nickname=#{nickname}, password=#{password}, avatar=#{avatar} WHERE id=#{id}")
    void update(User user);

    // 根据 ID 查用户 (为了获取旧密码)
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);
}