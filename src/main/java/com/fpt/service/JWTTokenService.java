package com.fpt.service;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fpt.dto.LoginInfoUser;
import com.fpt.entity.User;
import com.fpt.entity.UserStatus;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class JWTTokenService {
	
    private static final long EXPIRATION_TIME = 864000000; // 10 days
    private static final String SECRET = "123456";
    private static final String PREFIX_TOKEN = "Bearer";
    private static final String AUTHORIZATION = "Authorization";

    public static void addJWTTokenAndUserInfoToBody(HttpServletResponse response, User user) throws IOException {
        String JWT = Jwts.builder()
                .setSubject(user.getUserName())
                .claim("role", user.getRole())
                .claim("id", user.getId())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();

        LoginInfoUser userDto = new LoginInfoUser(
                user.getStatus().equals(UserStatus.ACTIVE) ? JWT : null,
                user.getUserName(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getId(),
                user.getStatus().toString()
        );

        // Tạo JSON object với code và user info
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", HttpServletResponse.SC_OK);
        responseBody.put("message", "Login successfully!");
        responseBody.put("user", userDto);

        // Convert object to JSON string
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(responseBody);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(json);
    }

    public static Authentication parseTokenToUserInformation(HttpServletRequest request) {
        String token = request.getHeader(AUTHORIZATION);
        if (token == null) return null;

        Claims claims = Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token.replace(PREFIX_TOKEN, "").trim())
                .getBody();

        String username = claims.getSubject();
        String role = claims.get("role", String.class); // lấy role từ token

        if (username != null && role != null) {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
            return new UsernamePasswordAuthenticationToken(username, null, List.of(authority));
        }

        return null;
    }

}