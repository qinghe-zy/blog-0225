package com.blog.blog_system.service;

import com.blog.blog_system.entity.User;
import com.blog.blog_system.mapper.UserMapper;
import com.blog.blog_system.mapper.VisitLogMapper; // ✨ 引入 VisitLogMapper
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*; // ✨ 引入集合工具类

/**
 * 用户业务逻辑层
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VisitLogMapper visitLogMapper; // ✨ 注入 VisitLogMapper

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

    /**
     * ✨✨✨ 新增：生成用户画像数据 (雷达图) ✨✨✨
     * @return Map<String, Object> 包含雷达图需要的 indicator(维度) 和 data(数值)
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