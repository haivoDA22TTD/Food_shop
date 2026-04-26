package com.example.foodshop.product.service;

import com.example.foodshop.product.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthContextService {
    private final JwtUtil jwtUtil;

    public AuthContextService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public AuthContext requireAuth(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Vui lòng đăng nhập");
        }

        String token = authHeader.substring(7);
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();
        Number userIdClaim = claims.get("userId", Number.class);
        if (username == null || userIdClaim == null) {
            throw new IllegalArgumentException("Token không hợp lệ");
        }

        return new AuthContext(username, userIdClaim.longValue());
    }

    public record AuthContext(String username, Long userId) {
    }
}
