package com.example.foodshop.interceptor;

import com.example.foodshop.service.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    @Autowired
    private RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String ipAddress = getClientIpAddress(request);

        // Skip rate limiting for static resources and health checks
        if (shouldSkipRateLimit(path)) {
            return true;
        }

        boolean allowed = true;
        String limitType = "general";

        // Apply specific rate limits based on endpoint
        if (path.equals("/api/auth/login") && method.equals("POST")) {
            allowed = rateLimitService.isLoginAllowed(ipAddress);
            limitType = "login";
        } else if (path.equals("/api/auth/register") && method.equals("POST")) {
            allowed = rateLimitService.isRegisterAllowed(ipAddress);
            limitType = "register";
        } else if (path.startsWith("/api/chatbot")) {
            String userId = getUserIdFromRequest(request);
            allowed = rateLimitService.isChatbotAllowed(userId);
            limitType = "chatbot";
        } else if (path.equals("/api/orders") && method.equals("POST")) {
            String userId = getUserIdFromRequest(request);
            allowed = rateLimitService.isOrderAllowed(userId);
            limitType = "order";
        } else if (path.startsWith("/api/reviews") && method.equals("POST")) {
            String userId = getUserIdFromRequest(request);
            allowed = rateLimitService.isReviewAllowed(userId);
            limitType = "review";
        } else if (path.startsWith("/api/")) {
            // General API rate limit
            allowed = rateLimitService.isApiAllowed(ipAddress);
            limitType = "api";
        }

        if (!allowed) {
            logger.warn("🚫 Rate limit exceeded - Type: {}, IP: {}, Path: {}", limitType, ipAddress, path);
            sendRateLimitResponse(response, limitType);
            return false;
        }

        return true;
    }

    private boolean shouldSkipRateLimit(String path) {
        return path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/img/") ||
               path.startsWith("/images/") ||
               path.startsWith("/fonts/") ||
               path.startsWith("/favicon.ico") ||
               path.equals("/actuator/health") ||
               path.equals("/actuator/info");
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private String getUserIdFromRequest(HttpServletRequest request) {
        // Try to get username from request attribute (set by JWT filter)
        Object principal = request.getAttribute("username");
        if (principal != null) {
            return principal.toString();
        }

        // Fallback to IP address for anonymous users
        return getClientIpAddress(request);
    }

    private void sendRateLimitResponse(HttpServletResponse response, String limitType) throws Exception {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Too Many Requests");
        errorResponse.put("message", getRateLimitMessage(limitType));
        errorResponse.put("status", 429);
        errorResponse.put("limitType", limitType);

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }

    private String getRateLimitMessage(String limitType) {
        return switch (limitType) {
            case "login" -> "Quá nhiều lần đăng nhập. Vui lòng thử lại sau 1 phút.";
            case "register" -> "Quá nhiều lần đăng ký. Vui lòng thử lại sau 1 giờ.";
            case "chatbot" -> "Bạn đang gửi tin nhắn quá nhanh. Vui lòng chờ một chút.";
            case "order" -> "Quá nhiều đơn hàng. Vui lòng thử lại sau 1 giờ.";
            case "review" -> "Quá nhiều đánh giá. Vui lòng thử lại sau 1 giờ.";
            case "api" -> "Quá nhiều yêu cầu. Vui lòng thử lại sau 1 phút.";
            default -> "Quá nhiều yêu cầu. Vui lòng thử lại sau.";
        };
    }
}
