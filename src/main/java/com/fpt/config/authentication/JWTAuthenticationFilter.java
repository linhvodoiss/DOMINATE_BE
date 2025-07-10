package com.fpt.config.authentication;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.authentication.CustomAuthenticationFailureHandler;
import com.fpt.dto.UserDTO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fpt.entity.User;
import com.fpt.service.IUserService;
import com.fpt.service.JWTTokenService;

public class JWTAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
	
	private IUserService userService;

    public JWTAuthenticationFilter(String url,
                                   AuthenticationManager authManager,
                                   IUserService userService,
                                   AuthenticationFailureHandler failureHandler) {
        super(new AntPathRequestMatcher(url));
        setAuthenticationManager(authManager);
        this.userService = userService;
        setAuthenticationFailureHandler(failureHandler);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        ObjectMapper objectMapper = new ObjectMapper();

        try {

            UserDTO loginRequest = objectMapper.readValue(request.getInputStream(), UserDTO.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUserName(),
                            loginRequest.getPassword(),
                            Collections.emptyList()
                    )
            );

        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid JSON format or missing fields\"}");
            response.getWriter().flush();
            return null;
        }
    }


    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult) throws IOException, ServletException {

        User user = userService.findUserByUserName(authResult.getName());
        JWTTokenService.addJWTTokenAndUserInfoToBody(response, user);
    }
}
