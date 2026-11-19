package com.lightfood.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightfood.common.constant.Role;
import com.lightfood.common.context.BaseContext;
import com.lightfood.common.context.LoginUser;
import com.lightfood.common.result.Result;
import com.lightfood.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.startsWith(BEARER_PREFIX)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "未登录或登录已过期");
            return false;
        }

        LoginUser user;
        try {
            user = jwtUtil.parse(header.substring(BEARER_PREFIX.length()));
        } catch (Exception ex) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "未登录或登录已过期");
            return false;
        }

        String expectedRole = request.getRequestURI().startsWith("/admin") ? Role.ADMIN : Role.USER;
        if (!expectedRole.equals(user.role())) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "没有访问权限");
            return false;
        }

        BaseContext.set(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        BaseContext.clear();
    }

    private void writeError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Result.error(status, message));
    }
}
