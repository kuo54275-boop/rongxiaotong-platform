package com.rongxiaotong.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.jwt.JWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT 工具：登录成功后发一个"令牌"给前端，前端以后每次请求带着它，
 * 后端验证令牌就知道是谁在操作（相当于一张 7 天有效的电子凭证）
 */
@Component
public class JwtUtil {

    @Value("${app.jwt-secret}")
    private String secret;

    private byte[] key;

    @PostConstruct
    public void init() {
        this.key = secret.getBytes();
    }

    /** 生成令牌，把用户信息打包进去 */
    public String createToken(Long userId, String role, String username) {
        return JWT.create()
                .setPayload("userId", userId)
                .setPayload("role", role)
                .setPayload("username", username)
                .setExpiresAt(DateUtil.offsetDay(new Date(), 7))   // 7 天有效
                .setKey(key)
                .sign();
    }

    /**
     * 解析令牌，返回其中的用户信息
     * @return null 表示令牌无效或已过期
     */
    public UserContext.LoginUser parseToken(String token) {
        try {
            JWT jwt = JWT.of(token).setKey(key);
            if (!jwt.verify()) {
                return null;
            }
            Long userId = Long.valueOf(String.valueOf(jwt.getPayload("userId")));
            String role = String.valueOf(jwt.getPayload("role"));
            String username = String.valueOf(jwt.getPayload("username"));
            return new UserContext.LoginUser(userId, role, username);
        } catch (Exception e) {
            return null;   // 令牌被篡改、格式错误、已过期，都视为无效
        }
    }
}
