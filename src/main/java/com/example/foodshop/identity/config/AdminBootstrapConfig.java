package com.example.foodshop.identity.config;

import com.example.foodshop.identity.service.UserService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminBootstrapConfig {

    @Bean
    public ApplicationRunner createDefaultAdminOnFirstRun(UserService userService) {
        return args -> userService.ensureDefaultAdmin();
    }
}
