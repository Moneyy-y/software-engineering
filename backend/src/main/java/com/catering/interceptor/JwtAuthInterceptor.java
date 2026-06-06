package com.catering.interceptor;

import com.catering.entity.User;
import com.catering.mapper.UserMapper;
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
    private final UserMapper userMapper;

    public JwtAuthInterceptor(JwtUtil jwtUtil, StringRedisTemplate redisTemplate, UserMapper userMapper) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.userMapper = userMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        boolean optionalAuth = isOptionalAuthPath(request.getMethod(), request.getRequestURI());
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            if (optionalAuth) {
                return true;
            }
            writeUnauthorized(response, "未登录");
            return false;
        }
        String token = auth.substring(7);
        try {
            if (isTokenBlacklisted(token)) {
                if (optionalAuth) {
                    return true;
                }
                writeUnauthorized(response, "Token已失效");
                return false;
            }
            Long userId = jwtUtil.getUserId(token);
            String role = jwtUtil.getRole(token);

            User user = userMapper.selectById(userId);
            if (user == null || user.getStatus() == null || user.getStatus() == 0) {
                if (optionalAuth) {
                    return true;
                }
                writeUnauthorized(response, "用户已被停用");
                return false;
            }

            request.setAttribute("userId", userId);
            request.setAttribute("role", role);
            return true;
        } catch (ExpiredJwtException e) {
            if (optionalAuth) {
                return true;
            }
            writeUnauthorized(response, "Token已过期");
            return false;
        } catch (Exception e) {
            if (optionalAuth) {
                return true;
            }
            writeUnauthorized(response, "认证失败");
            return false;
        }
    }

    /** 允许匿名访问，但若 token 有效仍会识别登录用户 */
    private boolean isOptionalAuthPath(String method, String path) {
        if (!"GET".equalsIgnoreCase(method) || path == null) {
            return false;
        }
        return path.matches("/api/post/\\d+") || path.matches("/api/post/\\d+/comments");
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
