package com.example.foodshop.identity.security;

import com.example.foodshop.identity.entity.User;
import com.example.foodshop.identity.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");
        
        User user = userService.processOAuth2User(email, name, googleId);
        
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());
        
        // Redirect to frontend with token
        String redirectUrl = String.format("/oauth2/redirect?token=%s", token);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
