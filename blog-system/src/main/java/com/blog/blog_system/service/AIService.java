package com.blog.blog_system.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AIService {

    @Value("${ai.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.deepseek.url}")
    private String apiUrl;

    @Value("${ai.deepseek.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 调用 DeepSeek 生成摘要和标签
     */
    public Map<String, String> generateSummaryAndTags(String content) {
        if (content == null || content.length() < 10) {
            return Map.of("error", "内容太短，无法分析");
        }

        // 1. 构造 Prompt (提示词)
        String prompt = String.format(
                "你是一个技术博客助手。请根据以下博客正文，生成一个简短的摘要（50字以内）和3个核心技术标签（逗号分隔）。" +
                        "正文内容（截取前1000字）：%s\n\n" +
                        "请严格按照以下JSON格式返回，不要包含Markdown标记：\n" +
                        "{\"summary\": \"这里是摘要...\", \"tags\": \"Java,Spring,Redis\"}",
                content.substring(0, Math.min(content.length(), 1000)) // 防止Token超限
        );

        // 2. 构造请求体 (OpenAI 兼容格式)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.7);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", prompt));
        requestBody.put("messages", messages);

        // 3. 设置 Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 4. 发送请求
        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            // 5. 解析结果
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String aiContent = root.path("choices").get(0).path("message").path("content").asText();

                // 清洗可能存在的 Markdown 代码块标记 (```json ... ```)
                aiContent = aiContent.replace("```json", "").replace("```", "").trim();

                // 解析 AI 返回的 JSON 字符串
                JsonNode resultNode = objectMapper.readTree(aiContent);
                Map<String, String> result = new HashMap<>();
                result.put("summary", resultNode.path("summary").asText());
                result.put("tags", resultNode.path("tags").asText());
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("summary", "AI分析失败: " + e.getMessage(), "tags", "Error");
        }
        return Map.of("summary", "AI 服务暂不可用", "tags", "");
    }
    /**
     * ✨✨✨ 新增：AI 推荐匹配度分析 ✨✨✨
     * 场景：判断“某篇文章”是否适合“某位用户”，并生成推荐理由
     *
     * @param userHistory 用户最近读过的文章标题 (例如："Java多线程, Spring Boot入门")
     * @param targetBlogTitle 待推荐的文章标题 (例如："JVM 内存模型详解")
     * @return Map 包含 score(推荐分) 和 reason(推荐理由)
     */
    public Map<String, Object> analyzeRecommendation(String userHistory, String targetBlogTitle) {
        // 1. 构造 Prompt (提示词)
        // 让 AI 扮演推荐算法工程师
        String prompt = String.format(
                "你是一个博客推荐系统的算法分析师。请根据用户的历史阅读兴趣，判断他是否会对目标文章感兴趣。\n" +
                        "【用户历史阅读】：%s\n" +
                        "【目标文章标题】：%s\n\n" +
                        "请分析匹配度，并严格按照以下 JSON 格式返回结果：\n" +
                        "{\"score\": 85, \"reason\": \"一句话简短理由(20字以内)\"}\n" +
                        "注意：score范围0-100，分数越高代表越推荐。如果两者毫无关联（如历史是做菜，目标是编程），请给低分。",
                userHistory, targetBlogTitle
        );

        // 2. 构造请求体 (复用之前的配置)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.5); // 稍微降低随机性，让评分更稳定

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", prompt));
        requestBody.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 3. 发送请求 & 解析结果
        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String aiContent = root.path("choices").get(0).path("message").path("content").asText();
                // 清洗 Markdown
                aiContent = aiContent.replace("```json", "").replace("```", "").trim();

                JsonNode resultNode = objectMapper.readTree(aiContent);
                Map<String, Object> result = new HashMap<>();
                result.put("score", resultNode.path("score").asDouble());
                result.put("reason", resultNode.path("reason").asText());
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 降级策略：如果 AI 挂了，返回默认值
            Map<String, Object> mock = new HashMap<>();
            mock.put("score", 50.0);
            mock.put("reason", "热门内容推荐");
            return mock;
        }
        return Map.of("score", 0.0, "reason", "服务不可用");
    }
}