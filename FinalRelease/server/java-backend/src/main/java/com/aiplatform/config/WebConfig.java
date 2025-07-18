package com.aiplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 暂时禁用CORS配置，避免冲突
    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    //     registry.addMapping("/**")
    //             .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000")
    //             .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    //             .allowedHeaders("*")
    //             .allowCredentials(true)
    //             .maxAge(3600);
    // }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 