package com.rongxiaotong.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rongxiaotong.common.Result;
import com.rongxiaotong.util.JwtUtil;
import com.rongxiaotong.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：像门卫一样检查每个请求是否带了有效令牌
 * 没带令牌或令牌无效 → 直接返回"请先登录"，不放行
 * 带了有效令牌 → 把用户信息放进 UserContext，放行
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 预检请求（OPTIONS）直接放行
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("token");
        UserContext.LoginUser user = token == null ? null : jwtUtil.parseToken(token);
        if (user == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, "请先登录")));
            return false;
        }

        UserContext.set(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();   // 请求结束，清理用户信息
    }
}
