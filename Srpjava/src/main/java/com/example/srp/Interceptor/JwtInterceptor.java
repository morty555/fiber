package com.example.srp.Interceptor;

import com.example.srp.properties.JwtProperties;
import com.example.srp.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // 放行OPTIONS预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        // 这里示例用 adminSecretKey，你可以根据业务调整用哪个秘钥
        String secretKey = jwtProperties.getUserSecretKey();
        log.info("JWT SecretKey used for parsing: {}", secretKey);
        log.info("JWT Token to parse: {}", token);

        Claims claims = JwtUtil.parseJWT(secretKey, token);

        // 这里设置 userId 方便后续Controller获取
        request.setAttribute("userId", claims.get("userId"));
        return true;
    }
}


