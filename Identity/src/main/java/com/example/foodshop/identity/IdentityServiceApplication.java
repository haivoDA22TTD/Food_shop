package com.example.foodshop.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class IdentityServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
        System.out.println("🚀 Identity Service is running!");
        System.out.println("📊 Port: 8081 (dev) / 10000 (prod)");
        System.out.println("🔐 Endpoints: /api/auth/**, /api/users/**, /api/passkey/**");
    }
}
