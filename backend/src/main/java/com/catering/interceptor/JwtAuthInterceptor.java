package com.catering.interceptor;

import com.catering.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    public JwtAuthInterceptor(JwtUtil jwtUtil, StringRedisTemplate redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录");
            return false;
        }
        String token = auth.substring(7);
        try {
            if (isTokenBlacklisted(token)) {
                writeUnauthorized(response, "Token已失效");
                return false;
            }
            Long userId = jwtUtil.getUserId(token);
            String role = jwtUtil.getRole(token);
            request.setAttribute("userId", userId);
            request.setAttribute("role", role);
            return true;
        } catch (ExpiredJwtException e) {
            writeUnauthorized(response, "Token已过期");
            return false;
        } catch (Exception e) {
            writeUnauthorized(response, "认证失败");
            return false;
        }
    }

    /** Redis 未启动时跳过黑名单检查，避免开发环境全部接口 401 */
    private boolean isTokenBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey("token:blacklist:" + token));
        } catch (Exception e) {
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) {
        try {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":1002,\"message\":\"" + msg + "\",\"success\":false,\"data\":null}");
        } catch (java.io.IOException ignored) {
            // response 已提交或客户端断开时忽略
        }
    }
}
