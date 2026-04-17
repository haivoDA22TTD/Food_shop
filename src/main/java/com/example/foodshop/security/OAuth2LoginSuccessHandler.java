package com.example.foodshop.security;

import com.example.foodshop.entity.User;
import com.example.foodshop.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.example.foodshop.service.CustomUserDetailsService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        // Get user info from Google
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");
        
        // Find or create user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(email.split("@")[0] + "_" + System.currentTimeMillis());
                    newUser.setPassword(""); // Empty password for OAuth users
                    newUser.setRole(User.Role.CUSTOMER);
                    newUser.setGoogleId(googleId);
                    
                    try {
                        return userRepository.save(newUser);
                    } catch (Exception e) {
                        // If save fails, try to find by googleId
                        return userRepository.findByGoogleId(googleId)
                                .orElseThrow(() -> new RuntimeException("Failed to create user: " + e.getMessage()));
                    }
                });
        
        // Update Google ID if not set
        if (user.getGoogleId() == null || user.getGoogleId().isEmpty()) {
            user.setGoogleId(googleId);
            userRepository.save(user);
        }
        
        // Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        
        // Redirect to frontend with token
        String redirectUrl = String.format("/oauth2/redirect?token=%s", token);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
