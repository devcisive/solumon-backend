package com.example.solumonbackend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")  // 모든 경로에 대해 해당 설정 적용
        .allowedOrigins("*")  // 허용할 프론트엔드 도메인
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD")  // 허용할 HTTP 메서드
        .allowCredentials(false)
        .allowedHeaders("*")  // 모든 헤더 허용
        .maxAge(3600);
  }
}
