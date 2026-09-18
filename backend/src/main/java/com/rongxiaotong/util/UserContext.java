package com.rongxiaotong.util;

import com.rongxiaotong.common.BizException;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 当前登录用户上下文：登录拦截器把用户信息放进 ThreadLocal，
 * 之后任何 Controller/Service 都能用 UserContext.get() 拿到"当前是谁在操作"
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    /** 登录用户信息（从 JWT 中解析出来） */
    @Data
    @AllArgsConstructor
    public static class LoginUser {
        private Long userId;
        private String role;
        private String username;
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    /** 获取当前登录用户；未登录则抛异常 */
    public static LoginUser get() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new BizException("请先登录");
        }
        return user;
    }

    public static Long getUserId() {
        return get().getUserId();
    }

    public static String getRole() {
        return get().getRole();
    }

    /** 判断当前用户是否属于某角色 */
    public static boolean isRole(String role) {
        return role.equals(get().getRole());
    }

    /** 请求结束后清理，防止内存泄漏 */
    public static void clear() {
        HOLDER.remove();
    }
}
