package com.blog.blog_system.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常捕获
 * 只要代码里抛出了异常，都会走到这里，转成优雅的 JSON 返回给前端
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        e.printStackTrace(); // 打印堆栈方便后台排查
        // 返回给前端的永远是 JSON，而不是 500 报错页面
        return Result.error(e.getMessage() != null ? e.getMessage() : "服务器未知错误");
    }
}