package com.blog.blog_system.utils;

import java.security.MessageDigest;

public class MD5Util {
    public static String encrypt(String source) {
        if (source == null) return null;
        try {
            // 简单的 MD5 加密，生产环境通常会加盐 (Salt)
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(source.getBytes("utf-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                // 转成 16 进制
                int val = b & 0xff;
                if (val < 16) sb.append("0");
                sb.append(Integer.toHexString(val));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}