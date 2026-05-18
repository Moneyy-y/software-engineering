package com.catering.context;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class UserContext {

    public static Long getUserId() {
        HttpServletRequest req = currentRequest();
        if (req == null) return null;
        return (Long) req.getAttribute("userId");
    }

    public static String getRole() {
        HttpServletRequest req = currentRequest();
        if (req == null) return null;
        return (String) req.getAttribute("role");
    }

    private static HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
