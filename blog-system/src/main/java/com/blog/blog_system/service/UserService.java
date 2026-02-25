package com.blog.blog_system.service;

import com.blog.blog_system.entity.User;
import com.blog.blog_system.mapper.UserMapper;
import com.blog.blog_system.mapper.VisitLogMapper;
import com.blog.blog_system.utils.MD5Util; // ✨ 引入 MD5 工具
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 用户业务逻辑层
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VisitLogMapper visitLogMapper;

    /**
     * 注册逻辑
     */
    public String register(User user) {
        User existUser = userMapper.findByUsername(user.getUsername());
        if (existUser != null) {
            return "注册失败：用户名已被占用！";
        }

        // ✨✨ 安全升级：密码加密存储
        user.setPassword(MD5Util.encrypt(user.getPassword()));

        // ✨✨ 个性化：如果没传头像，设置为空字符串 (防止数据库报错)
        if (user.getAvatar() == null) {
            user.setAvatar("");
        }

        userMapper.insert(user);
        return "注册成功！";
    }

    /**
     * 登录逻辑 (Service 层保留方法，虽然 Controller 目前直接用了 Mapper，保持逻辑一致性)
     */
    public String login(User user) {
        User existUser = userMapper.findByUsername(user.getUsername());

        if (existUser == null) {
            return "登录失败：用户不存在！";
        }

        // ✨✨ 安全升级：将输入的密码加密后，再与数据库比对
        String inputMd5 = MD5Util.encrypt(user.getPassword());

        if (!existUser.getPassword().equals(inputMd5)) {
            return "登录失败：密码错误！";
        }

        return "登录成功！";
    }

    /**
     * ✨✨✨ 新增：生成用户画像数据 (雷达图) ✨✨✨
     * (保持你的原代码不变)
     */
    public Map<String, Object> getUserRadar(Long userId) {
        // 1. 查出所有标签记录
        List<String> rawTags = visitLogMapper.selectViewedTags(userId);

        // 2. 统计频率
        Map<String, Integer> tagCount = new HashMap<>();
        for (String tagStr : rawTags) {
            // 兼容中文逗号
            String[] tags = tagStr.split("[,，]");
            for (String t : tags) {
                t = t.trim();
                if (!t.isEmpty()) {
                    tagCount.put(t, tagCount.getOrDefault(t, 0) + 1);
                }
            }
        }

        // 3. 排序并取前 6 个最常看的标签（防止雷达图太挤）
        List<Map.Entry<String, Integer>> list = new ArrayList<>(tagCount.entrySet());
        // 降序排序
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        // 4. 组装成 ECharts 需要的格式
        List<Map<String, Object>> indicators = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        int max = 0; // 用于设置雷达图的最大刻度
        int limit = Math.min(list.size(), 6); // 最多显示6个维度

        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> entry = list.get(i);
            String tagName = entry.getKey();
            Integer count = entry.getValue();

            // 动态设置最大值，让图好看点（比如最大值是10，那max设为15）
            if (count > max) max = count;
            values.add(count);

            Map<String, Object> indicator = new HashMap<>();
            indicator.put("name", tagName);
            indicator.put("max", max + 5); // 稍微留点余量
            indicators.add(indicator);
        }

        // 防御性编程：如果用户是个新号，什么都没看，给个默认数据防止图表报错
        if (indicators.isEmpty()) {
            Map<String, Object> defaultInd = new HashMap<>();
            defaultInd.put("name", "暂无数据");
            defaultInd.put("max", 10);
            indicators.add(defaultInd);
            values.add(0);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("indicators", indicators);
        result.put("values", values);
        return result;
    }
}