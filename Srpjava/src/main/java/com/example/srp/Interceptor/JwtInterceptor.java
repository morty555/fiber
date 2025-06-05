package com.example.srp.Interceptor;

import com.example.srp.constant.JwtClaimsConstant;
import com.example.srp.properties.JwtProperties;
import com.example.srp.utils.JwtUtil;
import com.example.srp.utils.ThreadLocalUtil;
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
        String secretKey = jwtProperties.getUserSecretKey();

        log.info("JWT SecretKey used for parsing: {}", secretKey);
        log.info("JWT Token to parse: {}", token);

        Claims claims = JwtUtil.parseJWT(secretKey, token);

        Object userIdObj = claims.get(JwtClaimsConstant.USER_ID);  // 与登录时保持一致
        if (userIdObj == null) {
            throw new RuntimeException("Token does not contain userId");
        }

        // 设置属性供 Controller 使用
        request.setAttribute(JwtClaimsConstant.USER_ID, userIdObj);
        ThreadLocalUtil.setCurrentId(Long.valueOf(userIdObj.toString()));  // ✅ 避免 null 报错
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        //  清理 ThreadLocal 防止内存泄露
        ThreadLocalUtil.remove();
    }
}
