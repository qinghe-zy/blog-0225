package com.blog.blog_system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 关键逻辑：告诉系统，凡是访问 /images/** 的请求，都去本地的 uploads 文件夹找
        // "file:" + System.getProperty("user.dir") 代表当前项目根目录
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");
    }
}