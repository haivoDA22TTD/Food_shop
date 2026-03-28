package com.example.foodshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /img/** to the img folder in project root
        registry.addResourceHandler("/img/**")
                .addResourceLocations("file:img/", "classpath:/static/img/");
    }
}
