package com.example.foodshop.security;

import com.example.foodshop.entity.User;
import com.example.foodshop.service.OAuth2UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.example.foodshop.service.CustomUserDetailsService;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuth2UserService oAuth2UserService;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            
            // Get user info from Google
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String googleId = oAuth2User.getAttribute("sub");
            
            log.info("OAuth2 login attempt - Email: {}, GoogleId: {}", email, googleId);
            
            if (email == null || googleId == null) {
                log.error("OAuth2 login failed - Missing email or googleId");
                response.sendRedirect("/login?error=oauth_failed");
                return;
            }
            
            // Find or create user using service
            User user;
            try {
                user = oAuth2UserService.findOrCreateUser(email, googleId, name);
            } catch (IllegalStateException e) {
                log.error("OAuth2 error: {}", e.getMessage());
                response.sendRedirect("/login?error=oauth_conflict");
                return;
            }
            
            // Check if account is locked
            if (user.getAccountLocked() != null && user.getAccountLocked()) {
                log.warn("Account locked: {}", user.getUsername());
                response.sendRedirect("/login?error=account_locked");
                return;
            }
            
            // Generate JWT token
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String token = jwtUtil.generateToken(userDetails);
            
            log.info("OAuth2 login successful for user: {}", user.getUsername());
            
            // Determine if we're in production (HTTPS) or development (HTTP)
            boolean isSecure = request.isSecure() || 
                              request.getHeader("X-Forwarded-Proto") != null && 
                              request.getHeader("X-Forwarded-Proto").equals("https");
            
            // Set token in HttpOnly cookie instead of URL
            Cookie authCookie = new Cookie("auth_token", token);
            authCookie.setHttpOnly(true);   // Prevent JavaScript access (XSS protection)
            authCookie.setSecure(isSecure); // Only use Secure flag on HTTPS
            authCookie.setPath("/");        // Available for entire application
            authCookie.setMaxAge(86400);    // 24 hours (same as JWT expiration)
            response.addCookie(authCookie);
            
            log.info("✅ Cookie set - Secure: {}, Path: /, MaxAge: 86400", isSecure);
            
            // Redirect to home page (token is in cookie, not URL)
            getRedirectStrategy().sendRedirect(request, response, "/");
            
        } catch (Exception e) {
            log.error("OAuth2 login error: ", e);
            response.sendRedirect("/login?error=oauth_error");
        }
    }
}
