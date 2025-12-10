package com.dineflow.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Áp dụng cho tất cả API
                .allowedOriginPatterns("*") // Cho phép tất cả nguồn truy cập (Frontend, Mobile, Web Bếp...)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Cho phép các thao tác
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}