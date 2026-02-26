package com.blog.blog_system.common;

import lombok.Data;

/**
 * 统一 API 响应结果封装
 * 所有 Controller 的返回值都应该是这个类的对象
 */
@Data
public class Result<T> {
    private Integer code; // 200=成功, 500=错误
    private String msg;   // 提示信息
    private T data;       // 数据载体

    public static <T> Result<T> success(T data) {
        return success(data, "操作成功");
    }

    public static <T> Result<T> success(T data, String msg) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.setCode(500);
        r.setMsg(msg);
        return r;
    }
}