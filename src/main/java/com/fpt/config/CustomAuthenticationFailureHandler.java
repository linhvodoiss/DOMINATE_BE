package com.fpt.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.exception.AccountBannedException;
import com.fpt.exception.AccountNotActivatedException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        org.springframework.security.core.AuthenticationException exception) throws IOException {

        Map<String, Object> error = new HashMap<>();
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        if (exception instanceof AccountNotActivatedException) {
            error.put("code", 401);
            error.put("message", "Tài khoản của bạn chưa được kích hoạt");
        } else if (exception instanceof AccountBannedException) {
            error.put("code", 403);
            error.put("message", "Tài khoản của bạn đã bị khóa");
        } else if (exception instanceof BadCredentialsException) {
            error.put("code", 401);
            error.put("message", "Tài khoản hoặc mật khẩu không đúng");
        } else {
            error.put("code", 401);
            error.put("message", "Đăng nhập thất bại");
        }

        response.getWriter().write(mapper.writeValueAsString(error));
    }
}
