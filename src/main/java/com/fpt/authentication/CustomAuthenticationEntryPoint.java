package com.fpt.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json");

        Map<String, Object> data = new HashMap<>();
        data.put("code", 401);
        data.put("error", "Unauthorized");
        data.put("message", "Bạn không có quyền hạn");
        data.put("path", request.getRequestURI());

        new ObjectMapper().writeValue(response.getOutputStream(), data);
    }
}
